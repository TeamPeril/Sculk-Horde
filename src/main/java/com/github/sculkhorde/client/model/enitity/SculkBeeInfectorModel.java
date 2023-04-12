package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkBeeInfectorEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkBeeInfectorModel extends AnimatedGeoModel<SculkBeeInfectorEntity> {
    @Override
    public ResourceLocation getModelLocation(SculkBeeInfectorEntity object) {
        return new ResourceLocation(SculkHorde.MOD_ID, "geo/sculk_bee.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(SculkBeeInfectorEntity object) {
        return new ResourceLocation(SculkHorde.MOD_ID, "textures/entity/sculk_bee.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(SculkBeeInfectorEntity animatable)
    {
        return new ResourceLocation(SculkHorde.MOD_ID, "animations/sculk_bee.animation.json");
    }
}
