package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkHatcherEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class SculkHatcherModel extends DefaultedEntityGeoModel<SculkHatcherEntity> {

    public SculkHatcherModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "sculk_hatcher"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkHatcherEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
