package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.EnderBubbleAttackModel;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.EnderBubbleAttackEntity;

import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;


public class EnderBubbleAttackRenderer extends GeoEntityRenderer<EnderBubbleAttackEntity> {

    public EnderBubbleAttackRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new EnderBubbleAttackModel());
    }


}
