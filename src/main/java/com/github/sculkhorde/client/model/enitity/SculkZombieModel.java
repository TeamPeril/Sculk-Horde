package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkZombieEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkZombieModel extends AnimatedGeoModel<SculkZombieEntity> {


    public static final ResourceLocation MODEL = new ResourceLocation(SculkHorde.MOD_ID, "geo/sculk_zombie.geo.json");
    public static final ResourceLocation TEXTURE = new ResourceLocation(SculkHorde.MOD_ID,
            "textures/entity/sculk_zombie.png");
    public static final ResourceLocation ANIMATIONS = new ResourceLocation(SculkHorde.MOD_ID,
            "animations/sculk_zombie.animations.json");

    @Override
    public ResourceLocation getModelResource(SculkZombieEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SculkZombieEntity object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SculkZombieEntity animatable) {
        return ANIMATIONS;
    }

}
