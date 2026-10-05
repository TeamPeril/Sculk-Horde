package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkBroodStrikerEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class SculkBroodStrikerModel extends DefaultedEntityGeoModel<SculkBroodStrikerEntity> {

    public SculkBroodStrikerModel() {
        super(ResourceLocation.fromNamespaceAndPath(SculkHorde.MOD_ID, "sculk_brood_spitter"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkBroodStrikerEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
