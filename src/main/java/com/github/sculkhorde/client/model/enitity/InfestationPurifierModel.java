package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.InfestationPurifierEntity;
import com.github.sculkhorde.common.entity.SculkSporeSpewerEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class InfestationPurifierModel extends AnimatedGeoModel<InfestationPurifierEntity> {
    @Override
    public ResourceLocation getModelLocation(InfestationPurifierEntity object) {
        return new ResourceLocation(SculkHorde.MOD_ID, "geo/infestation_purifier.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(InfestationPurifierEntity object) {
        return new ResourceLocation(SculkHorde.MOD_ID, "textures/entity/infestation_purifier.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(InfestationPurifierEntity animatable)
    {
        return new ResourceLocation(SculkHorde.MOD_ID, "animations/infestation_purifier.animation.json");
    }
}
