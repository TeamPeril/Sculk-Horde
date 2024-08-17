package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SoulFireProjectileModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulFireProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SoulFireProjectileRenderer extends GeoEntityRenderer<SoulFireProjectileEntity> {
    public SoulFireProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SoulFireProjectileModel());
    }
}
