package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkWitchModel;
import com.github.sculkhorde.common.entity.SculkWitchEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import mod.azure.azurelib.renderer.layer.AutoGlowingGeoLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;


public class SculkWitchRenderer extends GeoEntityRenderer<SculkWitchEntity> {


    public SculkWitchRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkWitchModel());
        this.addRenderLayer(new AutoGlowingGeoLayer(this));
    }

}
