package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.ElementalBreezeMagicCircleModel;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.ElementalBreezeMagicCircleAttackEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;


public class ElementalBreezeMagicCircleRenderer extends GeoEntityRenderer<ElementalBreezeMagicCircleAttackEntity> {

    public ElementalBreezeMagicCircleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ElementalBreezeMagicCircleModel());
    }


}
