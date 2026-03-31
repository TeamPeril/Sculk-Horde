package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkSoulReaperModel;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.AngelOfReapingEntity;
import com.github.sculkhorde.core.ModConfig;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class SculkSoulReaperRenderer extends GeoEntityRenderer<AngelOfReapingEntity> {

    public SculkSoulReaperRenderer(EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new SculkSoulReaperModel());
        if(!ModConfig.SERVER.enable_gpu_compatibility_mode.get()) {this.addRenderLayer(new AutoGlowingGeoLayer(this));}
    }
}
