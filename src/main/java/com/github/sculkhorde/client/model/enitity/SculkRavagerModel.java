package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkRavagerEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkRavagerModel extends AnimatedGeoModel<SculkRavagerEntity> {

    public static final ResourceLocation MODEL = new ResourceLocation(SculkHorde.MOD_ID, "geo/sculk_ravager.geo.json");
    public static final ResourceLocation TEXTURE = new ResourceLocation(SculkHorde.MOD_ID,
            "textures/entity/sculk_ravager.png");
    public static final ResourceLocation ANIMATIONS = new ResourceLocation(SculkHorde.MOD_ID,
            "animations/sculk_ravager.animations.json");

    @Override
    public ResourceLocation getModelResource(SculkRavagerEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SculkRavagerEntity object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SculkRavagerEntity animatable) {
        return ANIMATIONS;
    }


}
