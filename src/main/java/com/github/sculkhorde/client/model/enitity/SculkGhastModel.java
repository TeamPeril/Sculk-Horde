package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkGhastEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class SculkGhastModel extends DefaultedEntityGeoModel<SculkGhastEntity> {

    public SculkGhastModel() {
        super(ResourceLocation.fromNamespaceAndPath(SculkHorde.MOD_ID, "sculk_ghast"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkGhastEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }

}
