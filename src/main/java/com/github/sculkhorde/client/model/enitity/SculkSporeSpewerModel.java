package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkSporeSpewerEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.example.client.EntityResources;
import software.bernie.example.entity.LEEntity;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkSporeSpewerModel extends AnimatedGeoModel<SculkSporeSpewerEntity> {

    public static final ResourceLocation MODEL = new ResourceLocation(SculkHorde.MOD_ID, "geo/sculk_spore_spewer.geo.json");
    public static final ResourceLocation TEXTURE = new ResourceLocation(SculkHorde.MOD_ID,
            "textures/entity/sculk_spore_spewer.png");
    public static final ResourceLocation ANIMATIONS = new ResourceLocation(SculkHorde.MOD_ID,
            "animations/sculk_spore_spewer.animations.json");

    @Override
    public ResourceLocation getModelResource(SculkSporeSpewerEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SculkSporeSpewerEntity object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SculkSporeSpewerEntity animatable) {
        return ANIMATIONS;
    }
}
