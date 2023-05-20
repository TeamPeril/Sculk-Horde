package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkEndermanEntity;
import com.github.sculkhorde.common.entity.SculkZombieEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class SculkEndermanModel extends DefaultedEntityGeoModel<SculkEndermanEntity> {


    /**
     * Create a new instance of this model class.<br>
     * The asset path should be the truncated relative path from the base folder.<br>
     * E.G.
     * <pre>{@code
     * 	new ResourceLocation("myMod", "animals/red_fish")
     * }</pre>
     *
     */
    public SculkEndermanModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "sculk_enderman"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkEndermanEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }

}
