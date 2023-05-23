package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.ChaosTeleporationRiftModel;
import com.github.sculkhorde.common.entity.specialeffects.ChaosTeleporationRiftEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ChaosTeleporationRiftRenderer extends GeoEntityRenderer<ChaosTeleporationRiftEntity> {

    public ChaosTeleporationRiftRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ChaosTeleporationRiftModel());
    }

}
