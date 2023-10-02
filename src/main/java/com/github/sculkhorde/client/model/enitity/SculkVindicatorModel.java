package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkVindicatorEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkVindicatorModel extends AnimatedGeoModel<SculkVindicatorEntity>
{

    public static final ResourceLocation MODEL = new ResourceLocation(SculkHorde.MOD_ID, "geo/sculk_vindicator.geo.json");
    public static final ResourceLocation TEXTURE = new ResourceLocation(SculkHorde.MOD_ID,
            "textures/entity/sculk_vindicator.png");
    public static final ResourceLocation ANIMATIONS = new ResourceLocation(SculkHorde.MOD_ID,
            "animations/sculk_vindicator.animations.json");

    @Override
    public ResourceLocation getModelResource(SculkVindicatorEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SculkVindicatorEntity object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SculkVindicatorEntity animatable) {
        return ANIMATIONS;
    }


}
