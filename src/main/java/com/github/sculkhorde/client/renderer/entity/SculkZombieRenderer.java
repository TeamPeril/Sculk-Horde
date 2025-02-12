package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkZombieModel;
import com.github.sculkhorde.common.entity.SculkZombieEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import mod.azure.azurelib.renderer.layer.AutoGlowingGeoLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;


public class SculkZombieRenderer extends GeoEntityRenderer<SculkZombieEntity> {


    public SculkZombieRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkZombieModel());
        this.addRenderLayer(new AutoGlowingGeoLayer(this));
    }

}
