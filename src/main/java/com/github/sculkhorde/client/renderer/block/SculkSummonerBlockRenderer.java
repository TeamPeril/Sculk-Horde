package com.github.sculkhorde.client.renderer.block;

import com.github.sculkhorde.client.model.block.SculkSummonerModel;
import com.github.sculkhorde.common.blockentity.SculkSummonerBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class SculkSummonerBlockRenderer extends GeoBlockRenderer<SculkSummonerBlockEntity> {
    public SculkSummonerBlockRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {
        super(rendererDispatcherIn, new SculkSummonerModel());
    }
}
