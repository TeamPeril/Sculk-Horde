package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkEndermanModel;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkEndermanEntity;

import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;


public class SculkEndermanRenderer extends GeoEntityRenderer<SculkEndermanEntity> {


    public SculkEndermanRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkEndermanModel());
    }

}
