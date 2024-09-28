package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkSoulReaperModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SculkSoulReaperRenderer extends GeoEntityRenderer<SculkSoulReaperEntity> {

    public SculkSoulReaperRenderer(EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new SculkSoulReaperModel());
    }
}
