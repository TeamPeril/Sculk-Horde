package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.ChaosTeleporationRiftModel;
import com.github.sculkhorde.client.model.enitity.ScullkSpineSpikeModel;
import com.github.sculkhorde.common.entity.specialeffects.ChaosTeleporationRiftEntity;
import com.github.sculkhorde.common.entity.specialeffects.SculkSpineSpikeAttackEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SculkSpineSpikeRenderer extends GeoEntityRenderer<SculkSpineSpikeAttackEntity> {

    public SculkSpineSpikeRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ScullkSpineSpikeModel());
    }

}
