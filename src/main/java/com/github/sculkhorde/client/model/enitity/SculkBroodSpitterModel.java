package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkBroodSpitterEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class SculkBroodSpitterModel extends DefaultedEntityGeoModel<SculkBroodSpitterEntity> {


    public SculkBroodSpitterModel() {
        super(ResourceLocation.fromNamespaceAndPath(SculkHorde.MOD_ID, "sculk_brood_spitter"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkBroodSpitterEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
