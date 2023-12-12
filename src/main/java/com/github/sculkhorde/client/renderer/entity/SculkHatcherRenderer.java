package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkHatcherModel;
import com.github.sculkhorde.common.entity.SculkHatcherEntity;

import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SculkHatcherRenderer extends GeoEntityRenderer<SculkHatcherEntity> {

    public SculkHatcherRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkHatcherModel());
    }
}
