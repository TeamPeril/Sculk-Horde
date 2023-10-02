package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkCreeperEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkCreeperModel extends AnimatedGeoModel<SculkCreeperEntity> {

    public static final ResourceLocation MODEL = new ResourceLocation(SculkHorde.MOD_ID, "geo/sculk_creeper.geo.json");
    public static final ResourceLocation TEXTURE = new ResourceLocation(SculkHorde.MOD_ID,
            "textures/entity/sculk_creeper.png");
    public static final ResourceLocation ANIMATIONS = new ResourceLocation(SculkHorde.MOD_ID,
            "animations/sculk_creeper.animations.json");

    @Override
    public ResourceLocation getModelResource(SculkCreeperEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SculkCreeperEntity object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SculkCreeperEntity animatable) {
        return ANIMATIONS;
    }
}
