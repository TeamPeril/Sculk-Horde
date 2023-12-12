package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkMiteEntity;
import com.github.sculkhorde.core.SculkHorde;

import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class SculkMiteModel extends DefaultedEntityGeoModel<SculkMiteEntity> {


    public SculkMiteModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "sculk_mite"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkMiteEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
