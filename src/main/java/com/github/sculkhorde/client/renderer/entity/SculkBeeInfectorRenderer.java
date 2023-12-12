package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkBeeInfectorModel;
import com.github.sculkhorde.common.entity.SculkBeeInfectorEntity;

import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SculkBeeInfectorRenderer extends GeoEntityRenderer<SculkBeeInfectorEntity> {


    public SculkBeeInfectorRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkBeeInfectorModel());
    }
}
