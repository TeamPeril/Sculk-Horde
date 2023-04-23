package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.common.entity.SculkZombieEntity;
import com.github.sculkhorde.client.model.enitity.SculkZombieModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;


public class SculkZombieRenderer extends GeoEntityRenderer<SculkZombieEntity> {


    public SculkZombieRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkZombieModel());
    }

}
