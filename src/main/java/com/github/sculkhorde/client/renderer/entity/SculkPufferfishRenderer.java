package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkPufferfishModel;
import com.github.sculkhorde.common.entity.SculkPufferfishEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SculkPufferfishRenderer extends GeoEntityRenderer<SculkPufferfishEntity> {

    public SculkPufferfishRenderer(EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new SculkPufferfishModel());
    }

}
