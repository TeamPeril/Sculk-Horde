package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SoulBlastEntityAttackModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulBlastAttackEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;


public class SoulBlastAttackEntityRenderer extends GeoEntityRenderer<SoulBlastAttackEntity> {

    public SoulBlastAttackEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SoulBlastEntityAttackModel());
    }


}
