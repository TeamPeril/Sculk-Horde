package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkMiteEntity;
import com.github.sculkhorde.common.entity.SculkSpitterEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkSpitterModel extends AnimatedGeoModel<SculkSpitterEntity>
{
    public static final ResourceLocation MODEL = new ResourceLocation(SculkHorde.MOD_ID, "geo/sculk_spitter.geo.json");
    public static final ResourceLocation TEXTURE = new ResourceLocation(SculkHorde.MOD_ID,
            "textures/entity/sculk_spitter.png");
    public static final ResourceLocation ANIMATIONS = new ResourceLocation(SculkHorde.MOD_ID,
            "animations/sculk_spitter.animations.json");

    @Override
    public ResourceLocation getModelResource(SculkSpitterEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SculkSpitterEntity object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SculkSpitterEntity animatable) {
        return ANIMATIONS;
    }
}
