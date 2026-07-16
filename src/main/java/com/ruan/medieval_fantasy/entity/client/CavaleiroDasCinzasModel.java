package com.ruan.medieval_fantasy.entity.client;

import com.ruan.medieval_fantasy.ExampleMod;
import com.ruan.medieval_fantasy.entity.custom.CavaleiroDasCinzas;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CavaleiroDasCinzasModel extends GeoModel<CavaleiroDasCinzas> {

    @Override
    public ResourceLocation getModelResource(CavaleiroDasCinzas animatable) {
        return new ResourceLocation(ExampleMod.MODID, "geo/entity/cavaleiro_das_cinzas.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CavaleiroDasCinzas animatable) {
        return new ResourceLocation(ExampleMod.MODID, "textures/entity/cavaleiro_das_cinzas_gecko.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CavaleiroDasCinzas animatable) {
        return new ResourceLocation(ExampleMod.MODID, "animations/entity/cavaleiro_das_cinzas.animation.json");
    }
}
