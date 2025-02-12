package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.GolemOfWrathModel;
import com.github.sculkhorde.common.entity.GolemOfWrathEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;


public class GolemOfWrathRenderer extends GeoEntityRenderer<GolemOfWrathEntity> {


    public GolemOfWrathRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GolemOfWrathModel());
        //this.addRenderLayer(new AutoGlowingGeoLayer(this));
    }

}
