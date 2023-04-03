package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkRavagerEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkRavagerModel extends AnimatedGeoModel<SculkRavagerEntity> {
    @Override
    public ResourceLocation getModelLocation(SculkRavagerEntity object) {
        return new ResourceLocation(SculkHorde.MOD_ID, "geo/sculk_ravager.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(SculkRavagerEntity object) {
        return new ResourceLocation(SculkHorde.MOD_ID, "textures/entity/sculk_ravager.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(SculkRavagerEntity animatable)
    {
        return new ResourceLocation(SculkHorde.MOD_ID, "animations/sculk_ravager.animation.json");
    }
}
