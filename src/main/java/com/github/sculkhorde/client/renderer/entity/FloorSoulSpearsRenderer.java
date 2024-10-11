package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.FloorSoulSpearsModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.FloorSoulSpearsEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FloorSoulSpearsRenderer extends GeoEntityRenderer<FloorSoulSpearsEntity> {

    public FloorSoulSpearsRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FloorSoulSpearsModel());
    }

}
