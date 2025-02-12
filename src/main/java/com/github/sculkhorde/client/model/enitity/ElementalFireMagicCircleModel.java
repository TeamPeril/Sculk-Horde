package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.ElementalFireMagicCircleAttackEntity;
import com.github.sculkhorde.core.SculkHorde;
import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ElementalFireMagicCircleModel extends DefaultedEntityGeoModel<ElementalFireMagicCircleAttackEntity> {
    public ElementalFireMagicCircleModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "elemental_fire_magic_circle"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(ElementalFireMagicCircleAttackEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
