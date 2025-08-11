package com.lunazstudios.farmies.client.animation;

import com.lunazstudios.farmies.client.runtime.BBModel;

import java.util.*;

public class AnimationMachine {
    public static final class Playback {
        public final String clip;     // clip atual a renderizar
        public final boolean loop;    // se deve loopar
        public final float timeSec;   // tempo forçado (override) do clip
        public Playback(String clip, boolean loop, float timeSec) {
            this.clip = clip; this.loop = loop; this.timeSec = timeSec;
        }
    }

    public static final class Transition {
        public final String fromState, toState;
        public final String clipOnce; // ex: "opening" / "closing"
        public Transition(String fromState, String toState, String clipOnce) {
            this.fromState = fromState; this.toState = toState; this.clipOnce = clipOnce;
        }
    }

    private final Map<String,String> stateLoopClip = new HashMap<>(); // state -> looping clip (ex: closed->"closed")
    private final List<Transition> transitions = new ArrayList<>();
    private final BBModel model;

    // runtime
    private String currentState;      // estado lógico (open/closed)
    private String activeClip;        // clip em reprodução (opening/closing/open/closed)
    private boolean clipLoop;
    private long clipStartGT = 0L;    // gameTime do início do clip
    private String targetState;       // estado desejado (pelo servidor)
    private boolean dirtyTarget = false;

    public AnimationMachine(BBModel model) { this.model = model; }

    // Builder-like API
    public AnimationMachine state(String state, String loopingClip) {
        stateLoopClip.put(state, loopingClip);
        return this;
    }
    public AnimationMachine transition(String from, String to, String onceClip) {
        transitions.add(new Transition(from, to, onceClip));
        return this;
    }

    /** Inicializa a máquina num estado lógico */
    public void initAtState(String state, long nowGT) {
        this.currentState = state;
        String clip = stateLoopClip.getOrDefault(state, state);
        this.activeClip = clip;
        this.clipLoop = true;
        this.clipStartGT = nowGT;
        this.targetState = state;
        this.dirtyTarget = false;
    }

    /** Solicita troca de estado (chamar no servidor) */
    public void requestState(String state, long nowGT) {
        if (Objects.equals(targetState, state)) return;
        this.targetState = state;
        this.dirtyTarget = true;
        // Se há transição declarada, inicia o onceClip
        Transition t = findTransition(currentState, targetState);
        if (t != null) {
            this.activeClip = t.clipOnce;
            this.clipLoop = false;
            this.clipStartGT = nowGT;
        } else {
            // fallback: vai direto pro looping do destino
            goToLoopState(targetState, nowGT);
        }
    }

    private Transition findTransition(String from, String to) {
        for (var t : transitions) if (t.fromState.equals(from) && t.toState.equals(to)) return t;
        return null;
    }

    private void goToLoopState(String state, long nowGT) {
        this.currentState = state;
        String clip = stateLoopClip.getOrDefault(state, state);
        this.activeClip = clip;
        this.clipLoop = true;
        this.clipStartGT = nowGT;
        this.dirtyTarget = false;
    }

    /** Amostra para render: devolve clip/loop/time. Também cola no estado final quando a transição acaba. */
    public Playback sample(long nowGT, float partialTicks) {
        float duration = 0f;
        var def = model.clips.get(activeClip);
        if (def != null) duration = def.durationSec;

        float elapsed = ((nowGT - clipStartGT) + partialTicks) / 20f;
        float time = (duration > 0f)
                ? (clipLoop ? (elapsed % duration) : Math.min(elapsed, duration))
                : elapsed;

        // Se clip é once e acabou, cola no looping do alvo
        if (!clipLoop && duration > 0f && elapsed >= duration) {
            if (dirtyTarget && targetState != null) {
                goToLoopState(targetState, nowGT);
                // recomputa o time do clip final
                var finalDef = model.clips.get(activeClip);
                float finDur = (finalDef != null) ? finalDef.durationSec : 0f;
                float finElapsed = ((nowGT - clipStartGT) + partialTicks) / 20f;
                float finTime = (finDur > 0f) ? (finElapsed % finDur) : finElapsed;
                return new Playback(activeClip, true, finTime);
            }
        }
        return new Playback(activeClip, clipLoop, time);
    }

    // opcional: helpers
    public String getCurrentState() { return currentState; }
    public String getActiveClip() { return activeClip; }
    public long getClipStartGT() { return clipStartGT; }
}