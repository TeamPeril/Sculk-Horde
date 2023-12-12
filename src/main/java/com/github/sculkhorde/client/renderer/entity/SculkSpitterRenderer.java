package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkSpitterModel;
import com.github.sculkhorde.common.entity.SculkSpitterEntity;

import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SculkSpitterRenderer extends GeoEntityRenderer<SculkSpitterEntity> {

    public SculkSpitterRenderer(EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new SculkSpitterModel());
    }

}
