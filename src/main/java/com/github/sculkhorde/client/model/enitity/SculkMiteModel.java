package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkMiteEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkMiteModel extends AnimatedGeoModel<SculkMiteEntity> {

    public static final ResourceLocation MODEL = new ResourceLocation(SculkHorde.MOD_ID, "geo/sculk_mite.geo.json");
    public static final ResourceLocation TEXTURE = new ResourceLocation(SculkHorde.MOD_ID,
            "textures/entity/sculk_mite.png");
    public static final ResourceLocation ANIMATIONS = new ResourceLocation(SculkHorde.MOD_ID,
            "animations/sculk_mite.animations.json");

    @Override
    public ResourceLocation getModelResource(SculkMiteEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SculkMiteEntity object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SculkMiteEntity animatable) {
        return ANIMATIONS;
    }
}
