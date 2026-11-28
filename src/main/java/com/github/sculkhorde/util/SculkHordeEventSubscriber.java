package com.github.sculkhorde.util;

import com.github.sculkhorde.modding_api.SculkHordeEventHooks;
import net.neoforged.bus.api.SubscribeEvent;

public class SculkHordeEventSubscriber {

    @SubscribeEvent
    public static void onBlockInfest(SculkHordeEventHooks.BlockInfestationEventHook event)
    {
        // Chance to place a sculk node the block
        //SculkNodeBlock.tryPlaceSculkNode(event.level, event.blockPos, false);
        //NodeUtil.tryMoveOldestNodeTo(event.level, event.blockPos, false);
    }
}
