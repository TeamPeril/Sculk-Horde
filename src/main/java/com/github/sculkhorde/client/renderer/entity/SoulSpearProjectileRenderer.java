package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SoulSpearProjectileModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulSpearProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SoulSpearProjectileRenderer extends GeoEntityRenderer<SoulSpearProjectileEntity> {
    public SoulSpearProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SoulSpearProjectileModel());
    }
}