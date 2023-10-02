package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkMiteAggressorModel;
import com.github.sculkhorde.common.entity.SculkMiteAggressorEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;


public class SculkMiteAggressorRenderer extends GeoEntityRenderer<SculkMiteAggressorEntity> {


    public SculkMiteAggressorRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkMiteAggressorModel());
    }
}
