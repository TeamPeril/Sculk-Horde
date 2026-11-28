package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.boss.angel_of_reaping.ElementalIceMagicCircleAttackEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class ElementalIceMagicCircleModel extends DefaultedEntityGeoModel<ElementalIceMagicCircleAttackEntity> {
    public ElementalIceMagicCircleModel() {
        super(ResourceLocation.fromNamespaceAndPath(SculkHorde.MOD_ID, "elemental_ice_magic_circle"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(ElementalIceMagicCircleAttackEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
