package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SoulPoisonProjectileModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulPoisonProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SoulPoisonProjectileRenderer extends GeoEntityRenderer<SoulPoisonProjectileEntity> {
    public SoulPoisonProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SoulPoisonProjectileModel());
    }
}