package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SoulFlySwatterProjectileModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulFlySwatterProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SoulFlySwatterProjectileRenderer extends GeoEntityRenderer<SoulFlySwatterProjectileEntity> {
    public SoulFlySwatterProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SoulFlySwatterProjectileModel());
    }
}