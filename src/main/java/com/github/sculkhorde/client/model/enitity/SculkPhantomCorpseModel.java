package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkPhantomCorpseEntity;
import com.github.sculkhorde.core.SculkHorde;

import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class SculkPhantomCorpseModel extends DefaultedEntityGeoModel<SculkPhantomCorpseEntity> {
    public SculkPhantomCorpseModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "sculk_phantom"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkPhantomCorpseEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
