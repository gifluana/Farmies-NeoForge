package com.lunazstudios.farmies.client.model;

import com.google.gson.*;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.util.GsonHelper;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class FarmiesModelDeserializer extends BlockModel.Deserializer {
    public static final FarmiesModelDeserializer INSTANCE = new FarmiesModelDeserializer();

    @Override
    public List<BlockElement> getElements(JsonDeserializationContext context, JsonObject object) {
        try {
            List<BlockElement> list = new ArrayList<>();
            JsonArray raw = GsonHelper.getAsJsonArray(object, "components", new JsonArray());
            for (JsonElement element : raw) {
                list.add(this.readBlockElement(element, context));
            }
            return list;
        } catch (Exception e) {
            throw new JsonParseException(e);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private BlockElement readBlockElement(JsonElement element, JsonDeserializationContext context) throws Exception {
        JsonObject obj = element.getAsJsonObject();

        Vector3f from = getVector3f(obj, "from");
        Vector3f to   = getVector3f(obj, "to");
        JsonObject rotation = GsonHelper.getAsJsonObject(obj, "rotation", new JsonObject());
        float angle = GsonHelper.getAsFloat(rotation, "angle", 0F);

        JsonArray zero = new JsonArray();
        zero.add(0F); zero.add(0F); zero.add(0F);
        obj.add("from", zero);
        obj.add("to", zero);
        rotation.addProperty("angle", 0F);

        BlockElement e = context.deserialize(obj, BlockElement.class);
        BlockElementRotation r = e.rotation != null
                ? new BlockElementRotation(e.rotation.origin(), e.rotation.axis(), angle, e.rotation.rescale())
                : null;
        return new BlockElement(from, to, e.faces, r, e.shade);
    }

    private static Vector3f getVector3f(JsonObject obj, String key) {
        JsonArray arr = GsonHelper.getAsJsonArray(obj, key);
        if (arr.size() != 3) throw new JsonParseException("Expected 3 floats for " + key);
        return new Vector3f(arr.get(0).getAsFloat(), arr.get(1).getAsFloat(), arr.get(2).getAsFloat());
    }
}