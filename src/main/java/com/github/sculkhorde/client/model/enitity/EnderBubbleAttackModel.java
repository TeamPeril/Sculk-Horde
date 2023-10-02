package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.boss.sculk_enderman.ChaosTeleporationRiftEntity;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.EnderBubbleAttackEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class EnderBubbleAttackModel extends AnimatedGeoModel<EnderBubbleAttackEntity> {

    public static final ResourceLocation MODEL = new ResourceLocation(SculkHorde.MOD_ID, "geo/ender_bubble_attack.geo.json");
    public static final ResourceLocation TEXTURE = new ResourceLocation(SculkHorde.MOD_ID,
            "textures/entity/ender_bubble_attack.png");
    public static final ResourceLocation ANIMATIONS = new ResourceLocation(SculkHorde.MOD_ID,
            "animations/ender_bubble_attack.animations.json");

    @Override
    public ResourceLocation getModelResource(EnderBubbleAttackEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(EnderBubbleAttackEntity object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(EnderBubbleAttackEntity animatable) {
        return ANIMATIONS;
    }
}
