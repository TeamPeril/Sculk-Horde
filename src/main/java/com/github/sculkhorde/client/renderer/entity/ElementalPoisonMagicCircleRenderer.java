package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.ElementalPoisonMagicCircleModel;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.ElementalPoisonMagicCircleAttackEntity;
import com.github.sculkhorde.core.ModConfig;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;


public class ElementalPoisonMagicCircleRenderer extends GeoEntityRenderer<ElementalPoisonMagicCircleAttackEntity> {

    public ElementalPoisonMagicCircleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ElementalPoisonMagicCircleModel());
        if(!ModConfig.SERVER.enable_gpu_compatibility_mode.get()) {this.addRenderLayer(new AutoGlowingGeoLayer(this));}
    }


}
