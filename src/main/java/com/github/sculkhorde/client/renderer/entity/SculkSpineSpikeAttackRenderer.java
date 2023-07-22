package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.ScullkSpineSpikeAttackModel;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkSpineSpikeAttackEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SculkSpineSpikeAttackRenderer extends GeoEntityRenderer<SculkSpineSpikeAttackEntity> {

    public SculkSpineSpikeAttackRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ScullkSpineSpikeAttackModel());
    }

}
