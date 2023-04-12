package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkBeeHarvesterEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkBeeHarvesterModel extends AnimatedGeoModel<SculkBeeHarvesterEntity> {
    @Override
    public ResourceLocation getModelLocation(SculkBeeHarvesterEntity object) {
        return new ResourceLocation(SculkHorde.MOD_ID, "geo/sculk_bee.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(SculkBeeHarvesterEntity object) {
        return new ResourceLocation(SculkHorde.MOD_ID, "textures/entity/sculk_bee.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(SculkBeeHarvesterEntity animatable)
    {
        return new ResourceLocation(SculkHorde.MOD_ID, "animations/sculk_bee.animation.json");
    }
}
