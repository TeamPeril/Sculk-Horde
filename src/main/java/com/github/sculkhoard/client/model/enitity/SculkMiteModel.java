package com.github.sculkhoard.client.model.enitity;

import com.github.sculkhoard.common.entity.SculkMiteEntity;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkMiteModel extends AnimatedGeoModel<SculkMiteEntity> {
    @Override
    public ResourceLocation getModelLocation(SculkMiteEntity object) {
        return new ResourceLocation(SculkHoard.MOD_ID, "geo/sculk_mite.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(SculkMiteEntity object) {
        return new ResourceLocation(SculkHoard.MOD_ID, "textures/entity/sculk_mite.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(SculkMiteEntity animatable) {
        return null;
    }
}
