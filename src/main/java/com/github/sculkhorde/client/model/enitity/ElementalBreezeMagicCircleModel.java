package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.ElementalBreezeMagicCircleAttackAttackEntity;
import com.github.sculkhorde.core.SculkHorde;
import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ElementalBreezeMagicCircleModel extends DefaultedEntityGeoModel<ElementalBreezeMagicCircleAttackAttackEntity> {
    public ElementalBreezeMagicCircleModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "elemental_breeze_magic_circle"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(ElementalBreezeMagicCircleAttackAttackEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
