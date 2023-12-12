package com.github.sculkhorde.client.model.block;

import com.github.sculkhorde.common.blockentity.SculkSummonerBlockEntity;
import com.github.sculkhorde.core.SculkHorde;

import mod.azure.azurelib.model.DefaultedBlockGeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class SculkSummonerModel extends DefaultedBlockGeoModel<SculkSummonerBlockEntity>
{
    /**
     * Create a new instance of this model class.<br>
     * The asset path should be the truncated relative path from the base folder.<br>
     * E.G.
     * <pre>{@code
     * 	new ResourceLocation("myMod", "workbench/sawmill")
     * }</pre>
     *
     */
    public SculkSummonerModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "sculk_summoner"));
    }

    @Override
    public RenderType getRenderType(SculkSummonerBlockEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
