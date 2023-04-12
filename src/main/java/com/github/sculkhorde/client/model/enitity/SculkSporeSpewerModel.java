package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkSporeSpewerEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkSporeSpewerModel extends AnimatedGeoModel<SculkSporeSpewerEntity> {
    @Override
    public ResourceLocation getModelLocation(SculkSporeSpewerEntity object) {
        return new ResourceLocation(SculkHorde.MOD_ID, "geo/sculk_spore_spewer.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(SculkSporeSpewerEntity object) {
        return new ResourceLocation(SculkHorde.MOD_ID, "textures/entity/sculk_spore_spewer.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(SculkSporeSpewerEntity animatable)
    {
        return new ResourceLocation(SculkHorde.MOD_ID, "animations/sculk_spore_spewer.animation.json");
    }
}
