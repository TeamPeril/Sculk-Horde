package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkSpitterEntity;
import com.github.sculkhorde.core.SculkHorde;

import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class SculkSpitterModel extends DefaultedEntityGeoModel<SculkSpitterEntity>
{
    public SculkSpitterModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "sculk_spitter"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkSpitterEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
