package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.ElementalBreezeMagicCircleModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.ElementalBreezeMagicCircleAttackAttackEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;


public class ElementalBreezeMagicCircleRenderer extends GeoEntityRenderer<ElementalBreezeMagicCircleAttackAttackEntity> {

    public ElementalBreezeMagicCircleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ElementalBreezeMagicCircleModel());
    }


}
