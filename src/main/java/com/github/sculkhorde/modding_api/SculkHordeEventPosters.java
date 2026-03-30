package com.github.sculkhorde.modding_api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Cancelable;

public class SculkHordeEventPosters {

    @Cancelable
    public static class BlockInfestationEventHook extends net.minecraftforge.eventbus.api.Event
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
            MinecraftForge.EVENT_BUS.post(this);
        }

        @Override
        public boolean isCancelable() {
            return true;
        }
    }
}
