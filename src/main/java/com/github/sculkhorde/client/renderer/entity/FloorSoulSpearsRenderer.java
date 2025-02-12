package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.FloorSoulSpearsModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.FloorSoulSpearsAttackEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class FloorSoulSpearsRenderer extends GeoEntityRenderer<FloorSoulSpearsAttackEntity> {

    public FloorSoulSpearsRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FloorSoulSpearsModel());
    }

}
