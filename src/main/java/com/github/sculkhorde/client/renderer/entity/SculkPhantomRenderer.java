package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkPhantomModel;
import com.github.sculkhorde.common.entity.SculkPhantomEntity;

import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;


public class SculkPhantomRenderer extends GeoEntityRenderer<SculkPhantomEntity> {


    public SculkPhantomRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkPhantomModel());
    }

}
