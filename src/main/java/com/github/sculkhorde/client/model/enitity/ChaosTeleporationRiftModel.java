package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.boss.sculk_enderman.ChaosTeleporationRiftEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.GeckoLib;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class ChaosTeleporationRiftModel extends AnimatedGeoModel<ChaosTeleporationRiftEntity> {

    public static final ResourceLocation MODEL = new ResourceLocation(SculkHorde.MOD_ID, "geo/chaos_teleporation_rift.geo.json");
    public static final ResourceLocation TEXTURE = new ResourceLocation(SculkHorde.MOD_ID,
            "textures/entity/chaos_teleporation_rift.png");
    public static final ResourceLocation ANIMATIONS = new ResourceLocation(SculkHorde.MOD_ID,
            "animations/chaos_teleporation_rift.animations.json");

    @Override
    public ResourceLocation getModelResource(ChaosTeleporationRiftEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ChaosTeleporationRiftEntity object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ChaosTeleporationRiftEntity animatable) {
        return ANIMATIONS;
    }
}
