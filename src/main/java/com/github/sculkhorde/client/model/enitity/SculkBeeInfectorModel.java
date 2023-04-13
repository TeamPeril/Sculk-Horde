package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkBeeInfectorEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class SculkBeeInfectorModel extends DefaultedEntityGeoModel<SculkBeeInfectorEntity> {

    public SculkBeeInfectorModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "sculk_bee_infector"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkBeeInfectorEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
