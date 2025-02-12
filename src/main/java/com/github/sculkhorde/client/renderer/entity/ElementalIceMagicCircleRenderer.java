package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.ElementalIceMagicCircleModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.ElementalIceMagicCircleAttackEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;


public class ElementalIceMagicCircleRenderer extends GeoEntityRenderer<ElementalIceMagicCircleAttackEntity> {

    public ElementalIceMagicCircleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ElementalIceMagicCircleModel());
    }


}
