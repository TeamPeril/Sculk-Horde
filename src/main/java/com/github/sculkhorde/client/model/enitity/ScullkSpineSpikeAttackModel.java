package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkSpineSpikeAttackEntity;
import com.github.sculkhorde.core.SculkHorde;

import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ScullkSpineSpikeAttackModel extends DefaultedEntityGeoModel<SculkSpineSpikeAttackEntity> {
    public ScullkSpineSpikeAttackModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "sculk_spine_spike_attack"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkSpineSpikeAttackEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
