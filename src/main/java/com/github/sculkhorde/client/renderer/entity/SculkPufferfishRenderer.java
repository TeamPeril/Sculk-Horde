package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkPufferfishModel;
import com.github.sculkhorde.common.entity.SculkPufferfishEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SculkPufferfishRenderer extends GeoEntityRenderer<SculkPufferfishEntity> {

    public SculkPufferfishRenderer(EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new SculkPufferfishModel());
    }

}
