package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.InfestationPurifierEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.example.entity.ParasiteEntity;
import software.bernie.geckolib.GeckoLib;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class InfestationPurifierModel extends DefaultedEntityGeoModel<InfestationPurifierEntity> {
    public InfestationPurifierModel() {
        super(new ResourceLocation(GeckoLib.MOD_ID, "parasite"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(InfestationPurifierEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
