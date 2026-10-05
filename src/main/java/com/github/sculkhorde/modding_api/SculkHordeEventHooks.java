package com.github.sculkhorde.modding_api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.ICancellableEvent;

public class SculkHordeEventHooks {

    public static class BlockInfestationEventHook extends net.neoforged.bus.api.Event implements ICancellableEvent
    {
        public ServerLevel level;
        public BlockPos blockPos;
        public BlockState victimBlock;
        public BlockState infestedBlock;

        public BlockInfestationEventHook(ServerLevel level, BlockPos pos, BlockState victim, BlockState infested)
        {
            this.level = level;
            blockPos = pos;
            victimBlock = victim;
            infestedBlock = infested;
        }

        public void postEvent()
        {
            NeoForge.EVENT_BUS.post(this);
        }

    }
}
