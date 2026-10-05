package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkGuardianEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class SculkGuardianModel extends DefaultedEntityGeoModel<SculkGuardianEntity> {


    public SculkGuardianModel() {
        super(ResourceLocation.fromNamespaceAndPath(SculkHorde.MOD_ID, "sculk_guardian"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkGuardianEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
