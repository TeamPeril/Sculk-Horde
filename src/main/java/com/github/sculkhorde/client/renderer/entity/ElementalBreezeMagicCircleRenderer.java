package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.ElementalBreezeMagicCircleModel;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.ElementalBreezeMagicCircleAttackEntity;
import com.github.sculkhorde.core.ModConfig;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;


public class ElementalBreezeMagicCircleRenderer extends GeoEntityRenderer<ElementalBreezeMagicCircleAttackEntity> {

    public ElementalBreezeMagicCircleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ElementalBreezeMagicCircleModel());
        if(!ModConfig.SERVER.enable_gpu_compatibility_mode.get()) {this.addRenderLayer(new AutoGlowingGeoLayer(this));}
    }


}
