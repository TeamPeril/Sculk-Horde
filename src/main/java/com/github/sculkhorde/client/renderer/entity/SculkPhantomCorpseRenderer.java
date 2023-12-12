package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkPhantomCorpseModel;
import com.github.sculkhorde.common.entity.SculkPhantomCorpseEntity;

import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SculkPhantomCorpseRenderer extends GeoEntityRenderer<SculkPhantomCorpseEntity> {

    public SculkPhantomCorpseRenderer(EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new SculkPhantomCorpseModel());
    }

}
