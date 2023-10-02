package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkBeeHarvesterEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkBeeHarvesterModel extends AnimatedGeoModel<SculkBeeHarvesterEntity> {
    public static final ResourceLocation MODEL = new ResourceLocation(SculkHorde.MOD_ID, "geo/sculk_bee_harvester.geo.json");
    public static final ResourceLocation TEXTURE = new ResourceLocation(SculkHorde.MOD_ID,
            "textures/entity/sculk_bee_harvester.png");
    public static final ResourceLocation ANIMATIONS = new ResourceLocation(SculkHorde.MOD_ID,
            "animations/sculk_bee_harvester.animations.json");

    @Override
    public ResourceLocation getModelResource(SculkBeeHarvesterEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SculkBeeHarvesterEntity object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SculkBeeHarvesterEntity animatable) {
        return ANIMATIONS;
    }
}
