package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.FireBallProjectileModel;
import com.github.sculkhorde.common.entity.projectile.FireBallProjectileEntity;
import com.github.sculkhorde.core.ModConfig;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class FireBallProjectileRenderer extends GeoEntityRenderer<FireBallProjectileEntity> {
    public FireBallProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FireBallProjectileModel());
        if(!ModConfig.SERVER.enable_gpu_compatibility_mode.get()) {this.addRenderLayer(new AutoGlowingGeoLayer(this));}
    }
}