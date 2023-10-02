package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkSpineSpikeAttackEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkSpineSpikeAttackModel extends AnimatedGeoModel<SculkSpineSpikeAttackEntity> {

    public static final ResourceLocation MODEL = new ResourceLocation(SculkHorde.MOD_ID, "geo/sculk_spine_spike_attack.geo.json");
    public static final ResourceLocation TEXTURE = new ResourceLocation(SculkHorde.MOD_ID,
            "textures/entity/sculk_spine_spike_attack.png");
    public static final ResourceLocation ANIMATIONS = new ResourceLocation(SculkHorde.MOD_ID,
            "animations/sculk_spine_spike_attack.animations.json");

    @Override
    public ResourceLocation getModelResource(SculkSpineSpikeAttackEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SculkSpineSpikeAttackEntity object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SculkSpineSpikeAttackEntity animatable) {
        return ANIMATIONS;
    }
}
