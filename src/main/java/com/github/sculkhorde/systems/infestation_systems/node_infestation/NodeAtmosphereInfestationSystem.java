package com.github.sculkhorde.systems.infestation_systems.node_infestation;

import com.github.sculkhorde.common.blockentity.SculkNodeBlockEntity;
import com.github.sculkhorde.common.effect.DiseasedAtmosphereEffect;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.chunk_cursor_system.ChunkCursorInfector;
import com.github.sculkhorde.systems.gravemind_system.Gravemind;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

public class NodeAtmosphereInfestationSystem {
    // The parent tile entity
    protected SculkNodeBlockEntity parent = null;
    protected long timeOfLastInfestationTick = 0;
    protected long INFESTATION_TICK_COOLDOWN = TickUnits.convertMinutesToTicks(1);
    protected long timeOfLastDiseasedAtmosphereTick = 0;
    protected long DISEASED_ATMOSPHERE_TICK_COOLDOWN = TickUnits.convertSecondsToTicks(10);
    protected int currentBlockInfestationRadius = 1;


    public NodeAtmosphereInfestationSystem(SculkNodeBlockEntity parent) {
        this.parent = parent;
    }


    public void serverTick() {
        if(parent.getLevel() == null)
        {
            return;
        }


        applyDiseasedAtmosphereTick();
        blockInfestationTick();
    }

    protected void applyDiseasedAtmosphereTick()
    {
        if(Math.abs(parent.getLevel().getGameTime() - timeOfLastDiseasedAtmosphereTick) < DISEASED_ATMOSPHERE_TICK_COOLDOWN)
        {
            return;
        }
        timeOfLastDiseasedAtmosphereTick = parent.getLevel().getGameTime();

        for(Player player: ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
        {
            if(EntityAlgorithms.isLivingEntityExplicitDenyTarget(player))
            {
                return;
            }

            if(BlockAlgorithms.getBlockDistance(player.blockPosition(), parent.getBlockPos()) < currentBlockInfestationRadius)
            {
                DiseasedAtmosphereEffect.applyToEntity(player, TickUnits.convertSecondsToTicks(30));
            }
        }
    }

    protected void blockInfestationTick()
    {
        if(!ModConfig.SERVER.block_infestation_enabled.get())
        {
            return;
        }

        if(Math.abs(parent.getLevel().getGameTime() - timeOfLastInfestationTick) < INFESTATION_TICK_COOLDOWN && timeOfLastInfestationTick != 0)
        {
            return;
        }

        timeOfLastInfestationTick = parent.getLevel().getGameTime();
        //blockInfectionRectangle(currentBlockInfestationRadius);
        //SculkHorde.cursorSystem.createChunkCursorsRing((ServerLevel) parent.getLevel(), parent.getBlockPos(), currentBlockInfestationRadius, true);

        currentBlockInfestationRadius += 32;

        if(currentBlockInfestationRadius > Gravemind.MINIMUM_DISTANCE_BETWEEN_NODES)
        {
            currentBlockInfestationRadius = 50;
            parent.setActive(false);
        }
    }

    protected int blockInfectionRectangle(int radius) {

        ChunkCursorInfector infector = SculkHorde.cursorSystem.createChunkInfector()
                .level((ServerLevel) parent.getLevel())
                .center(parent.getBlockPos(), radius)
                .caveMode(false)
                .fillMode(false)
                .blocksPerTick(128)
                .fadeDistance(0);

        SculkHorde.chunkInfestationSystem.addChunkInfector(infector);
        return 0;
    }
}
