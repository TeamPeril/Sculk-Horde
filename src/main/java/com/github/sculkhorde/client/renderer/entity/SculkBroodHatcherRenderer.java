package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.renderer.layer.CompatibilityAwareAutoGlowingGeoLayer;
import com.github.sculkhorde.client.model.enitity.SculkBroodHatcherModel;
import com.github.sculkhorde.common.entity.SculkBroodHatcherEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;


public class SculkBroodHatcherRenderer extends GeoEntityRenderer<SculkBroodHatcherEntity> {


    public SculkBroodHatcherRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkBroodHatcherModel());
        this.addRenderLayer(new CompatibilityAwareAutoGlowingGeoLayer<>(this));
    }

}
