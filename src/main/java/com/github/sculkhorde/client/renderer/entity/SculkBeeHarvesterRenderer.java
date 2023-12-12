package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkBeeHarvesterModel;
import com.github.sculkhorde.common.entity.SculkBeeHarvesterEntity;

import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;


public class SculkBeeHarvesterRenderer extends GeoEntityRenderer<SculkBeeHarvesterEntity> {

    public SculkBeeHarvesterRenderer(EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new SculkBeeHarvesterModel());
    }
}
