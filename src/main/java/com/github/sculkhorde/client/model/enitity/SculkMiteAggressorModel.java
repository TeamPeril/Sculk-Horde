package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkMiteAggressorEntity;
import com.github.sculkhorde.core.SculkHorde;

import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class SculkMiteAggressorModel extends DefaultedEntityGeoModel<SculkMiteAggressorEntity> {


    public SculkMiteAggressorModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "sculk_mite_aggressor"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkMiteAggressorEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
