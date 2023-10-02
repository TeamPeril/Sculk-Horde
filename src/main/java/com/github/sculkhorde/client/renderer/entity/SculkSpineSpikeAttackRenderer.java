package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkSpineSpikeAttackModel;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkSpineSpikeAttackEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoProjectilesRenderer;


public class SculkSpineSpikeAttackRenderer extends GeoProjectilesRenderer<SculkSpineSpikeAttackEntity> {

    public SculkSpineSpikeAttackRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkSpineSpikeAttackModel());
    }

}
