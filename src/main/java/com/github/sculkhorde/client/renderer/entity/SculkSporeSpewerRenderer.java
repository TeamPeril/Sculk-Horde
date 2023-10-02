package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkSporeSpewerModel;
import com.github.sculkhorde.common.entity.SculkSporeSpewerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class SculkSporeSpewerRenderer extends GeoEntityRenderer<SculkSporeSpewerEntity> {

    public SculkSporeSpewerRenderer(EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new SculkSporeSpewerModel());
    }

}
