package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkVexModel;
import com.github.sculkhorde.common.entity.SculkVexEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SculkVexRenderer extends GeoEntityRenderer<SculkVexEntity> {

    public SculkVexRenderer(EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new SculkVexModel());
    }

}
