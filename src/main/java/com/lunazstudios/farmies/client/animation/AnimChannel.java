package com.lunazstudios.farmies.client.animation;

import com.lunazstudios.farmies.client.util.Interp;
import com.lunazstudios.farmies.client.util.Keyframe;
import com.lunazstudios.farmies.client.util.Target;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AnimChannel {
    public final String bone;
    public final Target target;
    private final List<Keyframe> frames = new ArrayList<>();

    public AnimChannel(String bone, Target target) {
        this.bone = bone;
        this.target = target;
    }

    public void add(Keyframe kf) { frames.add(kf); }

    public void sort() { frames.sort(Comparator.comparing(Keyframe::time)); }

    public Vector3f sample(float tTicks) {
        if (frames.isEmpty()) return new Vector3f();
        if (tTicks <= frames.getFirst().time()) {
            var k = frames.getFirst(); return new Vector3f(k.x(), k.y(), k.z());
        }
        if (tTicks >= frames.getLast().time()) {
            var k = frames.getLast(); return new Vector3f(k.x(), k.y(), k.z());
        }
        int hi = 1;
        while (hi < frames.size() && frames.get(hi).time() < tTicks) hi++;
        int lo = hi - 1;

        Keyframe a = frames.get(lo), b = frames.get(hi);
        float dt = b.time() - a.time();
        float alpha = dt == 0 ? 0 : (tTicks - a.time()) / dt;

        if (a.interp() == Interp.LINEAR) {
            return new Vector3f(
                    a.x() + (b.x() - a.x()) * alpha,
                    a.y() + (b.y() - a.y()) * alpha,
                    a.z() + (b.z() - a.z()) * alpha
            );
        }
        return new Vector3f(a.x(), a.y(), a.z());
    }
}