package com.github.sculkhorde.util;

import com.github.sculkhorde.common.block.SculkNodeBlock;
import com.github.sculkhorde.modding_api.SculkHordeEventPosters;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SculkHordeEventSubscriber {

    @SubscribeEvent
    public static void onBlockInfest(SculkHordeEventPosters.BlockInfestationEventHook event)
    {
        // Chance to place a sculk node the block
        SculkNodeBlock.tryPlaceSculkNode(event.level, event.blockPos, false);
    }
}
