package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkVindicatorEntity;
import com.github.sculkhorde.core.SculkHorde;

import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class SculkVindicatorModel extends DefaultedEntityGeoModel<SculkVindicatorEntity> {


    /**
     * Create a new instance of this model class.<br>
     * The asset path should be the truncated relative path from the base folder.<br>
     * E.G.
     * <pre>{@code
     * 	new ResourceLocation("myMod", "animals/red_fish")
     * }</pre>
     *
     */
    public SculkVindicatorModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "sculk_vindicator"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkVindicatorEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }

}
