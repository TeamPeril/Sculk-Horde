package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkZombieEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkZombieModel extends AnimatedGeoModel<SculkZombieEntity> {
    @Override
    public ResourceLocation getModelLocation(SculkZombieEntity object) {
        return new ResourceLocation(SculkHorde.MOD_ID, "geo/sculk_zombie.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(SculkZombieEntity object) {
        return new ResourceLocation(SculkHorde.MOD_ID, "textures/entity/sculk_zombie.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(SculkZombieEntity animatable)
    {
        return new ResourceLocation(SculkHorde.MOD_ID, "animations/sculk_zombie.animation.json");
    }
}
