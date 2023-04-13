package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkHatcherModel;
import com.github.sculkhorde.common.entity.SculkHatcherEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SculkHatcherRenderer extends GeoEntityRenderer<SculkHatcherEntity> {

    public SculkHatcherRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkHatcherModel());
    }
}
