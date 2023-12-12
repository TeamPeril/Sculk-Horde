package com.github.sculkhorde.client.renderer.block;

import com.github.sculkhorde.client.model.block.SculkSummonerModel;
import com.github.sculkhorde.common.blockentity.SculkSummonerBlockEntity;

import mod.azure.azurelib.renderer.GeoBlockRenderer;

public class SculkSummonerBlockRenderer extends GeoBlockRenderer<SculkSummonerBlockEntity> {
    public SculkSummonerBlockRenderer() {
        super(new SculkSummonerModel());
    }
}
