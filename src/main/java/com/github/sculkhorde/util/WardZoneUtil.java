package com.github.sculkhorde.util;

import com.github.sculkhorde.common.block.PerimeterWardEmitterBlock;
import com.github.sculkhorde.common.block.PerimeterWardRelayBlock;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.systems.debugger_system.DebuggerSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.Optional;

public class WardZoneUtil {

    public static final String isRelayingWardID = "isRelayingWard";
    public static final BooleanProperty IS_RELAYING_WARD = BooleanProperty.create(WardZoneUtil.isRelayingWardID);

    public static final String parentWardBlockPosID = "parentWardBlockPos";


    public static boolean canRelayWard(BlockState blockState)
    {
        return blockState.hasProperty(IS_RELAYING_WARD);
    }

    public static boolean isBlockRelayingWard(LevelReader level, BlockPos blockPos)
    {
        BlockState block = level.getBlockState(blockPos);

        if(!canRelayWard(block))
        {
            return false;
        }

        return block.getValue(IS_RELAYING_WARD);
    }

    /**
     * Will check all directions except the facing direction for a max of 32 blocks for another relay block.
     * If it finds one, it will return the position of that block.
     * If it doesn't find one, it will return null.
     * @return The position of the previous relay block, or null if it doesn't find one.
     */
    public static Optional<BlockPos> findPreviousRelay(LevelReader level, BlockPos worldPosition)
    {
        BlockState currentBlock = level.getBlockState(worldPosition);
        Direction facingDirection = currentBlock.getValue(PerimeterWardRelayBlock.FACING);

        // Check all directions except the facing direction
        for(Direction dir : Direction.values())
        {
            if(dir == facingDirection)
            {
                continue;
            }

            for(int i = 1; i <= 32; i++)
            {
                BlockPos checkPos = worldPosition.relative(dir, i);
                if(canRelayWard(level.getBlockState(checkPos)))
                {
                    DebuggerSystem.cursorDebuggerModule.logDebug("Relay at " + worldPosition.toShortString() + " found prev relay at " + checkPos.toShortString());
                    return Optional.of(checkPos);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Will check the facing direction for a max of 32 blocks for another relay block.
     * If it finds one, it will return the position of that block.
     * If it doesn't find one, it will return null.
     * @return The position of the previous relay block, or null if it doesn't find one.
     */
    public static Optional<BlockPos> findNextRelay(LevelReader level, BlockPos worldPosition)
    {
        BlockState currentBlock = level.getBlockState(worldPosition);

        Direction oppositeFacingDirection = currentBlock.getValue(PerimeterWardRelayBlock.FACING);
        for(int i = 1; i <= 32; i++)
        {
            BlockPos checkPos = worldPosition.relative(oppositeFacingDirection, i);
            if(canRelayWard(level.getBlockState(checkPos)))
            {
                DebuggerSystem.cursorDebuggerModule.logDebug("Relay at " + worldPosition.toShortString() + " found next relay at " + checkPos.toShortString());
                return Optional.of(checkPos);
            }
        }
        return Optional.empty();
    }

    public static ModSavedData.PerimeterWardZoneEntry getOrCreatePerimeterWardZone(BlockPos parentRelay)
    {
        if(ModSavedData.getSaveData().getPerimeterWardZoneEntries().containsKey(parentRelay))
        {
            return ModSavedData.getSaveData().getPerimeterWardZoneEntries().get(parentRelay);
        }

        DebuggerSystem.cursorDebuggerModule.logDebug("Creating ward zone for relay " + parentRelay.toShortString());
        return createZoneEntry(parentRelay);
    }

    public static boolean doesZoneExist(BlockPos parentRelay)
    {
        return ModSavedData.getSaveData().getPerimeterWardZoneEntries().containsKey(parentRelay);
    }

    public static ModSavedData.PerimeterWardZoneEntry createZoneEntry(BlockPos parentRelay)
    {
        ModSavedData.PerimeterWardZoneEntry zone = new ModSavedData.PerimeterWardZoneEntry(parentRelay);
        return zone;
    }

    public static boolean isPosInAnyWardZone(BlockPos pos)
    {
        for(ModSavedData.PerimeterWardZoneEntry zone : ModSavedData.getSaveData().getPerimeterWardZoneEntries().values())
        {
            if(zone.isPosInsideOfZone(pos))
            {
                return true;
            }
        }

        return false;
    }

    public static void updateAllZones()
    {
        for(ModSavedData.PerimeterWardZoneEntry zone : ModSavedData.getSaveData().getPerimeterWardZoneEntries().values())
        {
            zone.updateRelayPositions();
        }
    }

    public static Optional<BlockPos> getParent(LevelReader level, BlockPos pos)
    {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if(blockEntity == null)
        {
            return Optional.empty();
        }

        CompoundTag tag = blockEntity.saveWithoutMetadata();

        if(tag.contains(parentWardBlockPosID))
        {
            return Optional.of(BlockPos.of(tag.getLong(parentWardBlockPosID)));
        }

        return Optional.empty();
    }
}
