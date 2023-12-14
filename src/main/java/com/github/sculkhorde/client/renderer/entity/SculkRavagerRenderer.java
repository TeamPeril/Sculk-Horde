package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkRavagerModel;
import com.github.sculkhorde.common.entity.SculkRavagerEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SculkRavagerRenderer extends GeoEntityRenderer<SculkRavagerEntity>
{
    public SculkRavagerRenderer(EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new SculkRavagerModel());
    }
}
