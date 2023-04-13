package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkMiteEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

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
