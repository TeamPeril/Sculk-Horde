package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkBroodSpitterModel;
import com.github.sculkhorde.common.entity.SculkBroodSpitterEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;


public class SculkBroodSpitterRenderer extends GeoEntityRenderer<SculkBroodSpitterEntity> {


    public SculkBroodSpitterRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkBroodSpitterModel());
    }

}
