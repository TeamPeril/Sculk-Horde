package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkPhantomModel;
import com.github.sculkhorde.client.model.enitity.SculkZombieModel;
import com.github.sculkhorde.common.entity.SculkPhantomEntity;
import com.github.sculkhorde.common.entity.SculkZombieEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;


public class SculkPhantomRenderer extends GeoEntityRenderer<SculkPhantomEntity> {


    public SculkPhantomRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkPhantomModel());
    }

}
