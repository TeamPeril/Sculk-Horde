package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkRavagerModel;
import com.github.sculkhorde.common.entity.SculkRavagerEntity;

import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SculkRavagerRenderer extends GeoEntityRenderer<SculkRavagerEntity>
{
    public SculkRavagerRenderer(EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new SculkRavagerModel());
    }
}
