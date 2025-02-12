package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkSoulReaperModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import mod.azure.azurelib.renderer.layer.AutoGlowingGeoLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SculkSoulReaperRenderer extends GeoEntityRenderer<SculkSoulReaperEntity> {

    public SculkSoulReaperRenderer(EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new SculkSoulReaperModel());
        this.addRenderLayer(new AutoGlowingGeoLayer(this));
    }
}
