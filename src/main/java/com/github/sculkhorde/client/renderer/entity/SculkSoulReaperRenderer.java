package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.renderer.layer.CompatibilityAwareAutoGlowingGeoLayer;
import com.github.sculkhorde.client.model.enitity.SculkSoulReaperModel;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.AngelOfReapingEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SculkSoulReaperRenderer extends GeoEntityRenderer<AngelOfReapingEntity> {

    public SculkSoulReaperRenderer(EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new SculkSoulReaperModel());
        this.addRenderLayer(new CompatibilityAwareAutoGlowingGeoLayer<>(this));
    }
}
