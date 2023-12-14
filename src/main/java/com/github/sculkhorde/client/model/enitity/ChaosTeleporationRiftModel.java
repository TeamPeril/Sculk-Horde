package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.boss.sculk_enderman.ChaosTeleporationRiftEntity;
import com.github.sculkhorde.core.SculkHorde;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class ChaosTeleporationRiftModel extends DefaultedEntityGeoModel<ChaosTeleporationRiftEntity> {
    public ChaosTeleporationRiftModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "chaos_teleporation_rift"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(ChaosTeleporationRiftEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
