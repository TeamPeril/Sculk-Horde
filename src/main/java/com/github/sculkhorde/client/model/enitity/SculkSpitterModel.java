package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkSpitterEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkSpitterModel extends AnimatedGeoModel<SculkSpitterEntity> {
    @Override
    public ResourceLocation getModelLocation(SculkSpitterEntity object) {
        return new ResourceLocation(SculkHorde.MOD_ID, "geo/sculk_spitter.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(SculkSpitterEntity object) {
        return new ResourceLocation(SculkHorde.MOD_ID, "textures/entity/sculk_spitter.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(SculkSpitterEntity animatable) {
        return new ResourceLocation(SculkHorde.MOD_ID, "animations/sculk_spitter.animation.json");
    }
}
