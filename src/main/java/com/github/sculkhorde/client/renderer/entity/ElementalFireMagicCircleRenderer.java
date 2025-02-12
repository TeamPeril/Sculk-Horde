package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.ElementalFireMagicCircleModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.ElementalFireMagicCircleAttackEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;


public class ElementalFireMagicCircleRenderer extends GeoEntityRenderer<ElementalFireMagicCircleAttackEntity> {

    public ElementalFireMagicCircleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ElementalFireMagicCircleModel());
    }


}
