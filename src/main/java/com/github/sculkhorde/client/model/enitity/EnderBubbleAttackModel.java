package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.boss.sculk_enderman.EnderBubbleAttackEntity;
import com.github.sculkhorde.core.SculkHorde;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class EnderBubbleAttackModel extends DefaultedEntityGeoModel<EnderBubbleAttackEntity> {
    public EnderBubbleAttackModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "ender_bubble_attack"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(EnderBubbleAttackEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
