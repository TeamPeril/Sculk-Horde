package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkSalmonModel;
import com.github.sculkhorde.client.model.enitity.SculkZombieModel;
import com.github.sculkhorde.common.entity.SculkSalmonEntity;
import com.github.sculkhorde.common.entity.SculkZombieEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;


public class SculkSalmonRenderer extends GeoEntityRenderer<SculkSalmonEntity> {


    public SculkSalmonRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkSalmonModel());
    }

}
