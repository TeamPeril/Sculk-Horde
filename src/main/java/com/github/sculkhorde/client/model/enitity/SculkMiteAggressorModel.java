package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkMiteAggressorEntity;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkEndermanEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkMiteAggressorModel extends AnimatedGeoModel<SculkMiteAggressorEntity> {


    public static final ResourceLocation MODEL = new ResourceLocation(SculkHorde.MOD_ID, "geo/sculk_mite_aggressor.geo.json");
    public static final ResourceLocation TEXTURE = new ResourceLocation(SculkHorde.MOD_ID,
            "textures/entity/sculk_mite_aggressor.png");
    public static final ResourceLocation ANIMATIONS = new ResourceLocation(SculkHorde.MOD_ID,
            "animations/sculk_mite_aggressor.animations.json");

    @Override
    public ResourceLocation getModelResource(SculkMiteAggressorEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SculkMiteAggressorEntity object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SculkMiteAggressorEntity animatable) {
        return ANIMATIONS;
    }
}
