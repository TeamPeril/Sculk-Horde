package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkSalmonModel;
import com.github.sculkhorde.client.model.enitity.SculkZombieModel;
import com.github.sculkhorde.common.entity.SculkSalmonEntity;
import com.github.sculkhorde.common.entity.SculkZombieEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;


public class SculkSalmonRenderer extends GeoEntityRenderer<SculkSalmonEntity> {


    public SculkSalmonRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkSalmonModel());
    }

}
