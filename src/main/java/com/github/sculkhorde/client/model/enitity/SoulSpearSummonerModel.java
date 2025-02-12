package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulSpearSummonerAttackEntity;
import com.github.sculkhorde.core.SculkHorde;
import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class SoulSpearSummonerModel extends DefaultedEntityGeoModel<SoulSpearSummonerAttackEntity> {
    public SoulSpearSummonerModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "soul_spear_summoner"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SoulSpearSummonerAttackEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
