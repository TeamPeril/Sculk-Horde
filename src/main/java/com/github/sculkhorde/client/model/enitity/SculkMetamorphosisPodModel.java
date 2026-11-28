package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkMetamorphosisPodEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class SculkMetamorphosisPodModel extends DefaultedEntityGeoModel<SculkMetamorphosisPodEntity> {

    public SculkMetamorphosisPodModel() {
        super(ResourceLocation.fromNamespaceAndPath(SculkHorde.MOD_ID, "sculk_metamorphosis_pod"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkMetamorphosisPodEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }

}
