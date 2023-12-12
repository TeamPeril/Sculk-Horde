package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.InfestationPurifierModel;
import com.github.sculkhorde.common.entity.InfestationPurifierEntity;

import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;


public class InfestationPurifierRenderer extends GeoEntityRenderer<InfestationPurifierEntity> {

    public InfestationPurifierRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new InfestationPurifierModel());
    }


}
