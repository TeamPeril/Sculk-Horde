package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkBroodHatcherEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class SculkBroodHatcherModel extends DefaultedEntityGeoModel<SculkBroodHatcherEntity> {

    public SculkBroodHatcherModel() {
        super(ResourceLocation.fromNamespaceAndPath(SculkHorde.MOD_ID, "sculk_brood_hatcher"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkBroodHatcherEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }

}
