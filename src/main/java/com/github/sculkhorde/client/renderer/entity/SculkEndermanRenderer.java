package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkEndermanModel;
import com.github.sculkhorde.client.model.enitity.SculkZombieModel;
import com.github.sculkhorde.common.entity.SculkEndermanEntity;
import com.github.sculkhorde.common.entity.SculkZombieEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;


public class SculkEndermanRenderer extends GeoEntityRenderer<SculkEndermanEntity> {


    public SculkEndermanRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkEndermanModel());
    }

}
