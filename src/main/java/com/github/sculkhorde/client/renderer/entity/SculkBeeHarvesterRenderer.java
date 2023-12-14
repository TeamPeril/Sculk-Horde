package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkBeeHarvesterModel;
import com.github.sculkhorde.common.entity.SculkBeeHarvesterEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;


public class SculkBeeHarvesterRenderer extends GeoEntityRenderer<SculkBeeHarvesterEntity> {

    public SculkBeeHarvesterRenderer(EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new SculkBeeHarvesterModel());
    }
}
