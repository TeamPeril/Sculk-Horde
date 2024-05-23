package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkCreeperEntity;
import com.github.sculkhorde.common.entity.SculkSquidEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class SculkSquidModel extends DefaultedEntityGeoModel<SculkSquidEntity> {


    public SculkSquidModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "sculk_squid"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkSquidEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
