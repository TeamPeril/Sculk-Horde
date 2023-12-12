package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkCreeperEntity;
import com.github.sculkhorde.core.SculkHorde;

import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class SculkCreeperModel extends DefaultedEntityGeoModel<SculkCreeperEntity> {


    public SculkCreeperModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "sculk_creeper"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkCreeperEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
