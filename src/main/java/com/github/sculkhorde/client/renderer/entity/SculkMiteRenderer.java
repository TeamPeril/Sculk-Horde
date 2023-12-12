package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkMiteModel;
import com.github.sculkhorde.common.entity.SculkMiteEntity;

import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SculkMiteRenderer extends GeoEntityRenderer<SculkMiteEntity> {

    public SculkMiteRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkMiteModel());
    }

}
