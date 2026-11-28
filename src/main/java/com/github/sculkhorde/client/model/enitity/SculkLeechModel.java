package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkLeechEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class SculkLeechModel extends DefaultedEntityGeoModel<SculkLeechEntity> {

    public SculkLeechModel() {
        super(ResourceLocation.fromNamespaceAndPath(SculkHorde.MOD_ID, "sculk_leech"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkLeechEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }

}
