package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkBeeInfectorEntity;
import com.github.sculkhorde.core.SculkHorde;

import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class SculkBeeInfectorModel extends DefaultedEntityGeoModel<SculkBeeInfectorEntity> {

    public SculkBeeInfectorModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "sculk_bee"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkBeeInfectorEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
