package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkSpitterModel;
import com.github.sculkhorde.client.model.enitity.SculkSquidModel;
import com.github.sculkhorde.common.entity.SculkSpitterEntity;
import com.github.sculkhorde.common.entity.SculkSquidEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SculkSquidRenderer extends GeoEntityRenderer<SculkSquidEntity> {

    public SculkSquidRenderer(EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new SculkSquidModel());
    }

}
