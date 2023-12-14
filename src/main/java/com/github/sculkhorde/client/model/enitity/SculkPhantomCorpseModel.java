package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkPhantomCorpseEntity;
import com.github.sculkhorde.core.SculkHorde;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

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
