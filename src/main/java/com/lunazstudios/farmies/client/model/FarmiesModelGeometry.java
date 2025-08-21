package com.lunazstudios.farmies.client.model;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.neoforged.neoforge.client.model.ElementsModel;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

public class FarmiesModelGeometry implements IUnbakedGeometry<FarmiesModelGeometry> {
    private final BlockModel model;

    public FarmiesModelGeometry(BlockModel model) { this.model = model; }

    public static class Loader implements IGeometryLoader<FarmiesModelGeometry> {
        private static final Gson GSON = new GsonBuilder()
                .registerTypeAdapter(BlockModel.class, FarmiesModelDeserializer.INSTANCE)
                .registerTypeAdapter(BlockElement.class, new BlockElement.Deserializer())
                .registerTypeAdapter(BlockElementFace.class, new BlockElementFace.Deserializer())
                .registerTypeAdapter(BlockFaceUV.class, new BlockFaceUV.Deserializer())
                .registerTypeAdapter(ItemTransforms.class, new ItemTransforms.Deserializer())
                .registerTypeAdapter(ItemTransform.class, new ItemTransform.Deserializer())
                .create();

        @Override
        public FarmiesModelGeometry read(JsonObject json, com.google.gson.JsonDeserializationContext ctx) {
            BlockModel parsed = GSON.fromJson(json, BlockModel.class);
            return new FarmiesModelGeometry(parsed);
        }
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context,
                           ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter,
                           ModelState modelState,
                           ItemOverrides overrides) {
        ElementsModel elements = new ElementsModel(model.getElements());
        return elements.bake(
                context,
                baker,
                spriteGetter,
                modelState,
                model.getOverrides(baker, model, spriteGetter)
        );
    }
}