package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkBeeHarvesterEntity;
import com.github.sculkhorde.core.SculkHorde;

import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class SculkBeeHarvesterModel extends DefaultedEntityGeoModel<SculkBeeHarvesterEntity> {
    public SculkBeeHarvesterModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "sculk_bee"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkBeeHarvesterEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
