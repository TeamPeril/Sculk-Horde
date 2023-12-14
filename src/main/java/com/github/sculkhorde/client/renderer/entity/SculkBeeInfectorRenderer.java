package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkBeeInfectorModel;
import com.github.sculkhorde.common.entity.SculkBeeInfectorEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SculkBeeInfectorRenderer extends GeoEntityRenderer<SculkBeeInfectorEntity> {


    public SculkBeeInfectorRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkBeeInfectorModel());
    }
}
