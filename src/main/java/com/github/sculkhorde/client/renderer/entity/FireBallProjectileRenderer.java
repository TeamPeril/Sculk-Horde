package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.AcidBlobProjectileModel;
import com.github.sculkhorde.client.model.enitity.FireBallProjectileModel;
import com.github.sculkhorde.common.entity.projectile.AcidBlobProjectileEntity;
import com.github.sculkhorde.common.entity.projectile.FireBallProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FireBallProjectileRenderer extends GeoEntityRenderer<FireBallProjectileEntity> {
    public FireBallProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FireBallProjectileModel());
    }
}