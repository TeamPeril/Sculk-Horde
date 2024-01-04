package com.github.sculkhorde.client.model.block;

import com.github.sculkhorde.common.blockentity.SculkSummonerBlockEntity;
import com.github.sculkhorde.common.blockentity.SoulHarvesterBlockEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;

public class SoulHarvesterModel extends DefaultedBlockGeoModel<SoulHarvesterBlockEntity>
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
    public SoulHarvesterModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "soul_harvester"));
    }

    @Override
    public RenderType getRenderType(SoulHarvesterBlockEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
