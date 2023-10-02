package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.InfestationPurifierModel;
import com.github.sculkhorde.common.entity.InfestationPurifierEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;


public class InfestationPurifierRenderer extends GeoEntityRenderer<InfestationPurifierEntity> {

    public InfestationPurifierRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new InfestationPurifierModel());
    }


}
