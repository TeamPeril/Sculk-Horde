package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkCreeperModel;
import com.github.sculkhorde.common.entity.SculkCreeperEntity;

import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;


public class SculkCreeperRenderer extends GeoEntityRenderer<SculkCreeperEntity> {


    public SculkCreeperRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkCreeperModel());
    }

}
