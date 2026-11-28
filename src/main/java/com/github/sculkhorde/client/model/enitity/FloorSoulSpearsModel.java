package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.boss.angel_of_reaping.FloorSoulSpearsAttackEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class FloorSoulSpearsModel extends DefaultedEntityGeoModel<FloorSoulSpearsAttackEntity> {
    public FloorSoulSpearsModel() {
        super(ResourceLocation.fromNamespaceAndPath(SculkHorde.MOD_ID, "floor_soul_spears"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(FloorSoulSpearsAttackEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
