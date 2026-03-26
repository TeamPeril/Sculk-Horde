package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.ElementalPoisonMagicCircleModel;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.ElementalPoisonMagicCircleAttackEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;


public class ElementalPoisonMagicCircleRenderer extends GeoEntityRenderer<ElementalPoisonMagicCircleAttackEntity> {

    public ElementalPoisonMagicCircleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ElementalPoisonMagicCircleModel());
    }


}
