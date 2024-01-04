package com.github.sculkhorde.client.renderer.block;

import com.github.sculkhorde.client.model.block.SculkSummonerModel;
import com.github.sculkhorde.client.model.block.SoulHarvesterModel;
import com.github.sculkhorde.common.blockentity.SculkSummonerBlockEntity;
import com.github.sculkhorde.common.blockentity.SoulHarvesterBlockEntity;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class SoulHarvesterBlockRenderer extends GeoBlockRenderer<SoulHarvesterBlockEntity> {
    public SoulHarvesterBlockRenderer() {
        super(new SoulHarvesterModel());
    }
}
