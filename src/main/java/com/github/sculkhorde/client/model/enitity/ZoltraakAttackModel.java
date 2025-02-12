package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.ZoltraakAttackEntity;
import com.github.sculkhorde.core.SculkHorde;
import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ZoltraakAttackModel extends DefaultedEntityGeoModel<ZoltraakAttackEntity> {
    public ZoltraakAttackModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "zoltraak_attack_entity"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(ZoltraakAttackEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
