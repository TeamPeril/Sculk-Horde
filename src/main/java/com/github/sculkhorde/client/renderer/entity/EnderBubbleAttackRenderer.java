package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.EnderBubbleAttackModel;
import com.github.sculkhorde.common.entity.specialeffects.EnderBubbleAttackEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;


public class EnderBubbleAttackRenderer extends GeoEntityRenderer<EnderBubbleAttackEntity> {

    public EnderBubbleAttackRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new EnderBubbleAttackModel());
    }


}
