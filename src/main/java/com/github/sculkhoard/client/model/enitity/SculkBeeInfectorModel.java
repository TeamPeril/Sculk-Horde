package com.github.sculkhoard.client.model.enitity;

import com.github.sculkhoard.common.entity.SculkBeeInfectorEntity;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkBeeInfectorModel extends AnimatedGeoModel<SculkBeeInfectorEntity> {
    @Override
    public ResourceLocation getModelLocation(SculkBeeInfectorEntity object) {
        return new ResourceLocation(SculkHoard.MOD_ID, "geo/sculk_bee_temp.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(SculkBeeInfectorEntity object) {
        return new ResourceLocation(SculkHoard.MOD_ID, "textures/entity/sculk_bee_temp.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(SculkBeeInfectorEntity animatable) {
        return null;
    }
}
