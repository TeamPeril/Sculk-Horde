package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkSporeSpewerEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class SculkSporeSpewerModel extends DefaultedEntityGeoModel<SculkSporeSpewerEntity> {
    public SculkSporeSpewerModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "sculk_spore_spewer"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkSporeSpewerEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
