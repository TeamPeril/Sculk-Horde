package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SoulSpearSummonerModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulSpearSummonerAttackEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SoulSpearSummonerRenderer extends GeoEntityRenderer<SoulSpearSummonerAttackEntity> {

    public SoulSpearSummonerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SoulSpearSummonerModel());
    }

}
