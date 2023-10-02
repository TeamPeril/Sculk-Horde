package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkEndermanEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkEndermanModel extends AnimatedGeoModel<SculkEndermanEntity> {


    public static final ResourceLocation MODEL = new ResourceLocation(SculkHorde.MOD_ID, "geo/sculk_spore_spewer.geo.json");
    public static final ResourceLocation TEXTURE = new ResourceLocation(SculkHorde.MOD_ID,
            "textures/entity/sculk_spore_spewer.png");
    public static final ResourceLocation ANIMATIONS = new ResourceLocation(SculkHorde.MOD_ID,
            "animations/sculk_enderman.animations.json");

    @Override
    public ResourceLocation getModelResource(SculkEndermanEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SculkEndermanEntity object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SculkEndermanEntity animatable) {
        return ANIMATIONS;
    }

}
