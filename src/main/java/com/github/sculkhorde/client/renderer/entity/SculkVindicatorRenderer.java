package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkVindicatorModel;
import com.github.sculkhorde.common.entity.SculkVindicatorEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import mod.azure.azurelib.renderer.layer.AutoGlowingGeoLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;


public class SculkVindicatorRenderer extends GeoEntityRenderer<SculkVindicatorEntity> {


    public SculkVindicatorRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkVindicatorModel());
        this.addRenderLayer(new AutoGlowingGeoLayer(this));
    }

}
