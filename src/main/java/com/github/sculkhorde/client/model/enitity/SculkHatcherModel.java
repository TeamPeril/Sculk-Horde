package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkHatcherEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkHatcherModel extends AnimatedGeoModel<SculkHatcherEntity> {

    public static final ResourceLocation MODEL = new ResourceLocation(SculkHorde.MOD_ID, "geo/sculk_hatcher.geo.json");
    public static final ResourceLocation TEXTURE = new ResourceLocation(SculkHorde.MOD_ID,
            "textures/entity/sculk_hatcher.png");
    public static final ResourceLocation ANIMATIONS = new ResourceLocation(SculkHorde.MOD_ID,
            "animations/sculk_hatcher.animations.json");

    @Override
    public ResourceLocation getModelResource(SculkHatcherEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SculkHatcherEntity object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SculkHatcherEntity animatable) {
        return ANIMATIONS;
    }
}
