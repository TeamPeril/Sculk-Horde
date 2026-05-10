package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.ElementalFireMagicCircleModel;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.ElementalFireMagicCircleAttackEntity;
import com.github.sculkhorde.core.ModConfig;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;


public class ElementalFireMagicCircleRenderer extends GeoEntityRenderer<ElementalFireMagicCircleAttackEntity> {

    public ElementalFireMagicCircleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ElementalFireMagicCircleModel());
        if(!ModConfig.SERVER.enable_gpu_compatibility_mode.get()) {this.addRenderLayer(new AutoGlowingGeoLayer(this));}
    }


}
