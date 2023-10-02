package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.InfestationPurifierEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class InfestationPurifierModel extends AnimatedGeoModel<InfestationPurifierEntity> {

    public static final ResourceLocation MODEL = new ResourceLocation(SculkHorde.MOD_ID, "geo/infestation_purifier.geo.json");
    public static final ResourceLocation TEXTURE = new ResourceLocation(SculkHorde.MOD_ID,
            "textures/entity/infestation_purifier.png");
    public static final ResourceLocation ANIMATIONS = new ResourceLocation(SculkHorde.MOD_ID,
            "animations/infestation_purifier.animations.json");

    @Override
    public ResourceLocation getModelResource(InfestationPurifierEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(InfestationPurifierEntity object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(InfestationPurifierEntity animatable) {
        return ANIMATIONS;
    }
}
