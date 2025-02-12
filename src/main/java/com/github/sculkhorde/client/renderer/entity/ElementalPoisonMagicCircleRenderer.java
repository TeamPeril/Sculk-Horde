package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.ElementalPoisonMagicCircleModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.ElementalPoisonMagicCircleAttackEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;


public class ElementalPoisonMagicCircleRenderer extends GeoEntityRenderer<ElementalPoisonMagicCircleAttackEntity> {

    public ElementalPoisonMagicCircleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ElementalPoisonMagicCircleModel());
    }


}
