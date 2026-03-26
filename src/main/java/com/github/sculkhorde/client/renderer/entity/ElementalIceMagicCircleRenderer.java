package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.ElementalIceMagicCircleModel;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.ElementalIceMagicCircleAttackEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;


public class ElementalIceMagicCircleRenderer extends GeoEntityRenderer<ElementalIceMagicCircleAttackEntity> {

    public ElementalIceMagicCircleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ElementalIceMagicCircleModel());
    }


}
