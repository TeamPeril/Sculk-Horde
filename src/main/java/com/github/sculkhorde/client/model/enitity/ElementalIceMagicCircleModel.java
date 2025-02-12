package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.ElementalIceMagicCircleAttackEntity;
import com.github.sculkhorde.core.SculkHorde;
import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;


public class ElementalIceMagicCircleModel extends DefaultedEntityGeoModel<ElementalIceMagicCircleAttackEntity> {
    public ElementalIceMagicCircleModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "elemental_ice_magic_circle"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(ElementalIceMagicCircleAttackEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
