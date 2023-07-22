package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkSpineSpikeAttackEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class SculkSpineSpikeAttackModel extends DefaultedEntityGeoModel<SculkSpineSpikeAttackEntity> {
    public SculkSpineSpikeAttackModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "sculk_spine_spike_attack"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkSpineSpikeAttackEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
