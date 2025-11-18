package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.SculkCreeperEntity;
import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.systems.event_system.EventSystem;
import com.github.sculkhorde.systems.event_system.events.RaidEvent.RaidEvent;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.function.Predicate;

public class BlowUpPriorityBlockGoal extends MoveToBlockGoal {
    protected final TagKey<Block> blockWithTagToRemove = ModBlocks.BlockTags.SCULK_RAID_TARGET_HIGH_PRIORITY;
    protected final SculkCreeperEntity removerMob;
    protected int ticksSinceReachedGoal;
    protected int distanceRequired;
    protected int ticksRequiredToBreakBlock;

    protected int searchCoolDownTicks = TickUnits.convertSecondsToTicks(5);
    protected int searchCoolDownTicksRemaining = 0;
    protected boolean hasReachedTarget = false;

    public BlowUpPriorityBlockGoal(SculkCreeperEntity sculkCreeperEntity, double p_25842_, int p_25843_, int distanceRequired, int ticksRequiredToBreakBlock) {
        super(sculkCreeperEntity, p_25842_, 24, p_25843_);
        this.removerMob = sculkCreeperEntity;
        this.distanceRequired = distanceRequired;
        this.ticksRequiredToBreakBlock = ticksRequiredToBreakBlock;
    }

    public boolean canUse()
    {
        searchCoolDownTicksRemaining--;

        if(searchCoolDownTicksRemaining <= 0)
        {
            findNearestBlock();
            searchCoolDownTicksRemaining = searchCoolDownTicks;
        }

        if(!((ISculkSmartEntity)removerMob).isParticipatingInRaid())
        {
            return false;
        }
        else if(blockPos == null || blockPos == BlockPos.ZERO)
        {
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {

        Optional<RaidEvent> raidEvent = EventSystem.getNearestRaidEvent((ServerLevel) mob.level(), mob.blockPosition());

        if(raidEvent.isEmpty())
        {
            return false;
        }

        if(raidEvent.get().isAreaAlreadyBlownUp(blockPos) && BlockAlgorithms.getBlockDistance(raidEvent.get().getObjectiveLocation(), blockPos) > 3)
        {
            return false;
        }

        return super.canContinueToUse();
    }

    @Override
    public void tick() {

        Level level = this.removerMob.level();
        BlockPos mobPosition = this.removerMob.blockPosition();
        BlockPos targetBlock = this.blockPos;

        if(targetBlock == null)
        {
            clearTargetBlock();
            return;
        }
        else if(!isBlockRaidTarget(level.getBlockState(targetBlock)))
        {
            clearTargetBlock();
            return;
        }
        else if(!targetBlock.closerThan(mobPosition, distanceRequired))
        {
            hasReachedTarget = true;
        }

        this.mob.getNavigation().moveTo((double)((float)targetBlock.getX()) + 0.5D, (double)targetBlock.getY(), (double)((float)targetBlock.getZ()) + 0.5D, this.speedModifier);

        // Once we reach target, there is no stopping the explosion
        if(hasReachedTarget)
        {
            this.removerMob.setSwellDir(1);
            ticksSinceReachedGoal++;
        }

        if (this.ticksSinceReachedGoal > ticksRequiredToBreakBlock)
        {
            this.removerMob.explodeSculkCreeper();
            Optional<RaidEvent> event = EventSystem.getNearestRaidEvent((ServerLevel) level, targetBlock);
            if(event.isEmpty())
            {
                return;
            }

            // Only advance to next objective if object blown up is
            if(BlockAlgorithms.getBlockDistanceXZ(removerMob.blockPosition(), event.get().getObjectiveLocation()) < 4)
            {
                event.get().advanceToNextObjective();
            }
            else
            {
                event.get().getAlreadyBlewUpTargets().add(removerMob.blockPosition());
            }
        }
    }

    protected void clearTargetBlock()
    {
        blockPos = null;
    }

    protected static boolean isBlockRaidTarget(BlockState blockState)
    {
        return blockState.is(ModBlocks.BlockTags.SCULK_RAID_TARGET_HIGH_PRIORITY) || blockState.is(ModBlocks.BlockTags.SCULK_RAID_TARGET_MEDIUM_PRIORITY);
    }

    protected static boolean isBlockEqualOrHigherPriorityThanCurrentTarget(BlockState objectiveBlockState, BlockState blockState)
    {

        // If current target is high priority, and blockState is high priority, return true
        if(objectiveBlockState.is(ModBlocks.BlockTags.SCULK_RAID_TARGET_HIGH_PRIORITY) && blockState.is(ModBlocks.BlockTags.SCULK_RAID_TARGET_HIGH_PRIORITY))
        {
            return true;
        }
        // If current target is medium and blockState is high, return true
        else if(objectiveBlockState.is(ModBlocks.BlockTags.SCULK_RAID_TARGET_MEDIUM_PRIORITY) && blockState.is(ModBlocks.BlockTags.SCULK_RAID_TARGET_HIGH_PRIORITY))
        {
            return true;
        }
        // If current target is medium and blockState is medium, return true
        else if(objectiveBlockState.is(ModBlocks.BlockTags.SCULK_RAID_TARGET_MEDIUM_PRIORITY) && blockState.is(ModBlocks.BlockTags.SCULK_RAID_TARGET_MEDIUM_PRIORITY))
        {
            return true;
        }
        // If current target is low and blockState is high, return true
        else if(objectiveBlockState.is(ModBlocks.BlockTags.SCULK_RAID_TARGET_LOW_PRIORITY) && blockState.is(ModBlocks.BlockTags.SCULK_RAID_TARGET_HIGH_PRIORITY))
        {
            return true;
        }
        // If current target is low and blockState is medium, return true
        else if(objectiveBlockState.is(ModBlocks.BlockTags.SCULK_RAID_TARGET_LOW_PRIORITY) && blockState.is(ModBlocks.BlockTags.SCULK_RAID_TARGET_MEDIUM_PRIORITY))
        {
            return true;
        }
        // If current target is low and blockState is low, return true
        else if(objectiveBlockState.is(ModBlocks.BlockTags.SCULK_RAID_TARGET_LOW_PRIORITY) && blockState.is(ModBlocks.BlockTags.SCULK_RAID_TARGET_LOW_PRIORITY))
        {
            return true;
        }

        return false;
    }

    protected boolean isValidTarget(LevelReader levelReader, BlockState blockState) {
        Optional<RaidEvent> nearestRaid = EventSystem.getNearestRaidEvent((ServerLevel) mob.level(), mob.blockPosition());

        if(nearestRaid.isEmpty())
        {
            return false;
        }

        if(!isBlockRaidTarget(blockState))
        {
            return false;
        }

        return isBlockEqualOrHigherPriorityThanCurrentTarget(levelReader.getBlockState(nearestRaid.get().getObjectiveLocation()), blockState);
    }

    // New Predicate for isValidTarget
    public final Predicate<BlockState> IS_VALID_TARGET = (blockState) -> {
        return isValidTarget(this.mob.level(), blockState);
    };

    @Override
    protected boolean findNearestBlock() {
        Optional<BlockPos> optionalTargetBlock = BlockAlgorithms.findBlockInCube((ServerLevel) this.mob.level(), this.mob.blockPosition(), IS_VALID_TARGET, 16);

        if(optionalTargetBlock.isEmpty())
        {
            return false;
        }


        Optional<RaidEvent> raidEvent = EventSystem.getNearestRaidEvent((ServerLevel) mob.level(), mob.blockPosition());

        if(raidEvent.isEmpty())
        {
            return false;
        }

        if(raidEvent.get().isAreaAlreadyBlownUp(optionalTargetBlock.get()) && BlockAlgorithms.getBlockDistance(raidEvent.get().getObjectiveLocation(), optionalTargetBlock.get()) > 3)
        {
            return false;
        }

        blockPos = optionalTargetBlock.get();

        return true;
    }

    @Override
    protected boolean isValidTarget(LevelReader p_25619_, BlockPos p_25620_) {
        return false;
    }
}

