package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.FloorSoulSpearsModel;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.FloorSoulSpearsAttackEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FloorSoulSpearsRenderer extends GeoEntityRenderer<FloorSoulSpearsAttackEntity> {

    public FloorSoulSpearsRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FloorSoulSpearsModel());
    }

}
