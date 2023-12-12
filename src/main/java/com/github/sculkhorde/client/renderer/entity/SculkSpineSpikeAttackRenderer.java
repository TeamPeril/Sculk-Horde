package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.ScullkSpineSpikeAttackModel;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkSpineSpikeAttackEntity;

import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SculkSpineSpikeAttackRenderer extends GeoEntityRenderer<SculkSpineSpikeAttackEntity> {

    public SculkSpineSpikeAttackRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ScullkSpineSpikeAttackModel());
    }

}
