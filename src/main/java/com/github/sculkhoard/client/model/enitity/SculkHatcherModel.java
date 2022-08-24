package com.github.sculkhoard.client.model.enitity;

import com.github.sculkhoard.common.entity.SculkHatcherEntity;
import com.github.sculkhoard.common.entity.SculkZombieEntity;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkHatcherModel extends AnimatedGeoModel<SculkHatcherEntity> {
    @Override
    public ResourceLocation getModelLocation(SculkHatcherEntity object) {
        return new ResourceLocation(SculkHoard.MOD_ID, "geo/sculk_hatcher.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(SculkHatcherEntity object) {
        return new ResourceLocation(SculkHoard.MOD_ID, "textures/entity/sculk_hatcher.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(SculkHatcherEntity animatable)
    {
        return new ResourceLocation(SculkHoard.MOD_ID, "animations/sculk_hatcher.animation.json");
    }
}
