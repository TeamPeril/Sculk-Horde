package com.github.sculkhoard.client.model.enitity;

import com.github.sculkhoard.common.entity.SculkSpitterEntity;
import com.github.sculkhoard.common.entity.SculkZombieEntity;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkSpitterModel extends AnimatedGeoModel<SculkSpitterEntity> {
    @Override
    public ResourceLocation getModelLocation(SculkSpitterEntity object) {
        return new ResourceLocation(SculkHoard.MOD_ID, "geo/sculk_spitter.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(SculkSpitterEntity object) {
        return new ResourceLocation(SculkHoard.MOD_ID, "textures/entity/sculk_spitter.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(SculkSpitterEntity animatable) {
        return new ResourceLocation(SculkHoard.MOD_ID, "animations/sculk_spitter.animation.json");
    }
}
