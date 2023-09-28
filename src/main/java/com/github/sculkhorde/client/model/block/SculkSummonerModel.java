package com.github.sculkhorde.client.model.block;

import com.github.sculkhorde.common.blockentity.SculkSummonerBlockEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.example.block.tile.HabitatTileEntity;
import software.bernie.example.client.EntityResources;
import software.bernie.geckolib3.GeckoLib;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SculkSummonerModel extends AnimatedGeoModel<SculkSummonerBlockEntity>
{

    @Override
    public ResourceLocation getAnimationResource(SculkSummonerBlockEntity entity) {
        return new ResourceLocation(SculkHorde.MOD_ID, "animations/block/sculk_summoner.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(SculkSummonerBlockEntity animatable) {
        return new ResourceLocation(SculkHorde.MOD_ID, "geo/sculk_summoner.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SculkSummonerBlockEntity entity) {
        return new ResourceLocation(SculkHorde.MOD_ID, "textures/block/sculk_summoner.png");
    }
}
