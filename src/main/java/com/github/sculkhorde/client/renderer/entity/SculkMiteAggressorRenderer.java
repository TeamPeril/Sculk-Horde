package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkMiteAggressorModel;
import com.github.sculkhorde.common.entity.SculkMiteAggressorEntity;

import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SculkMiteAggressorRenderer extends GeoEntityRenderer<SculkMiteAggressorEntity> {


    public SculkMiteAggressorRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkMiteAggressorModel());
    }
}
