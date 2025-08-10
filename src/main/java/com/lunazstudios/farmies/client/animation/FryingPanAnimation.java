package com.lunazstudios.farmies.client.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lunazstudios.farmies.client.util.Interp;
import com.lunazstudios.farmies.client.util.Keyframe;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FryingPanAnimation {

    public static float length = 1f;
    public static final List<Keyframe> positionFrames = new ArrayList<>();
    public static final List<Keyframe> rotationFrames = new ArrayList<>();

    static {
        ResourceLocation anim = ResourceLocation.fromNamespaceAndPath("farmies", "animations/frying_pan.json");

        try (InputStream stream = Minecraft.getInstance()
                .getResourceManager()
                .getResource(anim)
                .get()
                .open()) {

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

            if (root.has("length")) {
                length = root.get("length").getAsFloat() * 20f;
            }

            JsonArray animations = root.getAsJsonArray("animations");
            for (JsonElement animElem : animations) {
                JsonObject obj = animElem.getAsJsonObject();
                String target = obj.get("target").getAsString().toLowerCase();
                JsonArray keyframes = obj.getAsJsonArray("keyframes");

                for (JsonElement frameElem : keyframes) {
                    JsonObject frame = frameElem.getAsJsonObject();

                    float t = frame.has("timestamp") ? frame.get("timestamp").getAsFloat() * 20f : 0f;

                    JsonArray targetArr = frame.getAsJsonArray("target");
                    float x = targetArr.get(0).getAsFloat();
                    float y = targetArr.get(1).getAsFloat();
                    float z = targetArr.get(2).getAsFloat();

                    Interp interp = frame.has("interpolation")
                            ? Interp.valueOf(frame.get("interpolation").getAsString().toUpperCase())
                            : Interp.LINEAR;

                    Keyframe kf = new Keyframe(t, x, y, z, interp);

                    switch (target) {
                        case "position" -> positionFrames.add(kf);
                        case "rotation" -> rotationFrames.add(kf);
                    }
                }
            }

            positionFrames.sort(Comparator.comparing(Keyframe::time));
            rotationFrames.sort(Comparator.comparing(Keyframe::time));

        } catch (IOException e) {
            System.err.println("[Farmies] Error playing frying_pan.json animation: " + e.getMessage());
        }
    }
}