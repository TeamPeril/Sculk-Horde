package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkMiteModel;
import com.github.sculkhorde.common.entity.SculkMiteEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SculkMiteRenderer extends GeoEntityRenderer<SculkMiteEntity> {

    public SculkMiteRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkMiteModel());
    }

}
