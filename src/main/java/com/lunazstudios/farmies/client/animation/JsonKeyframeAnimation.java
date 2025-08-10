package com.lunazstudios.farmies.client.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lunazstudios.farmies.client.util.Interp;
import com.lunazstudios.farmies.client.util.Keyframe;
import com.lunazstudios.farmies.client.util.Target;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public abstract class JsonKeyframeAnimation {
    protected float lengthTicks = 20f;
    protected boolean loop = false;

    protected final Map<String, AnimChannel> position = new HashMap<>();
    protected final Map<String, AnimChannel> rotation = new HashMap<>();

    protected abstract ResourceLocation animationFile();

    protected JsonKeyframeAnimation() {
        loadFromJson();
    }

    private void loadFromJson() {
        ResourceLocation rl = animationFile();

        try (InputStream stream = Minecraft.getInstance()
                .getResourceManager()
                .getResource(rl).get().open();
             InputStreamReader reader = new InputStreamReader(stream)) {

            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            if (root.has("length")) {
                this.lengthTicks = root.get("length").getAsFloat() * 20f; // segundos -> ticks
            }
            if (root.has("loop")) {
                this.loop = root.get("loop").getAsBoolean();
            }

            JsonArray animations = root.getAsJsonArray("animations");
            for (JsonElement animElem : animations) {
                JsonObject obj = animElem.getAsJsonObject();

                String bone = obj.has("bone") ? obj.get("bone").getAsString() : "root";

                String targetStr = obj.get("target").getAsString().toLowerCase();
                Target target = switch (targetStr) {
                    case "position" -> Target.POSITION;
                    case "rotation" -> Target.ROTATION;
                    default -> throw new IllegalArgumentException("Target desconhecido: " + targetStr);
                };

                AnimChannel channel = (target == Target.POSITION)
                        ? position.computeIfAbsent(bone, b -> new AnimChannel(b, Target.POSITION))
                        : rotation.computeIfAbsent(bone, b -> new AnimChannel(b, Target.ROTATION));

                JsonArray keyframes = obj.getAsJsonArray("keyframes");
                for (JsonElement frameElem : keyframes) {
                    JsonObject frame = frameElem.getAsJsonObject();
                    float t = frame.has("timestamp") ? frame.get("timestamp").getAsFloat() * 20f : 0f;

                    JsonArray targetArr = frame.getAsJsonArray("target");
                    if (targetArr.size() < 3) continue;
                    float x = targetArr.get(0).getAsFloat();
                    float y = targetArr.get(1).getAsFloat();
                    float z = targetArr.get(2).getAsFloat();

                    Interp interp = frame.has("interpolation")
                            ? parseInterp(frame.get("interpolation").getAsString())
                            : Interp.LINEAR;

                    channel.add(new Keyframe(t, x, y, z, interp));
                }
            }

            position.values().forEach(AnimChannel::sort);
            rotation.values().forEach(AnimChannel::sort);

        } catch (Exception e) {
            System.err.println("[Farmies] Error loading animation " + rl + ": " + e);
        }
    }

    private Interp parseInterp(String s) {
        return switch (s.toLowerCase()) {
            case "linear" -> Interp.LINEAR;
            default -> Interp.LINEAR;
        };
    }

    public float lengthTicks() { return lengthTicks; }
    public boolean loop() { return loop; }

    public void sample(float tTicks, PoseConsumer out) {
        for (var e : position.entrySet()) {
            Vector3f p = e.getValue().sample(tTicks);
            out.acceptPosition(e.getKey(), p);
        }
        for (var e : rotation.entrySet()) {
            Vector3f r = e.getValue().sample(tTicks);
            out.acceptRotation(e.getKey(), r);
        }
    }

    public interface PoseConsumer {
        void acceptPosition(String bone, Vector3f pos);
        void acceptRotation(String bone, Vector3f rotDeg);
    }
}