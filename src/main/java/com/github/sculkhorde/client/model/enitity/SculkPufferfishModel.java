package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkPufferfishEntity;
import com.github.sculkhorde.core.SculkHorde;
import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class SculkPufferfishModel extends DefaultedEntityGeoModel<SculkPufferfishEntity> {

    public SculkPufferfishModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "sculk_pufferfish"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkPufferfishEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
