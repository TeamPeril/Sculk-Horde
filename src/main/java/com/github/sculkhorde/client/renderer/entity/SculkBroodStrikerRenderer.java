package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkBroodSpitterModel;
import com.github.sculkhorde.client.model.enitity.SculkBroodStrikerModel;
import com.github.sculkhorde.common.entity.SculkBroodSpitterEntity;
import com.github.sculkhorde.common.entity.SculkBroodStrikerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;


public class SculkBroodStrikerRenderer extends GeoEntityRenderer<SculkBroodStrikerEntity> {


    public SculkBroodStrikerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SculkBroodStrikerModel());
    }

}
