package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.SculkCreeperEntity;
import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.core.ItemRegistry;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Predicate;

public class BlowUpPriorityBlockGoal extends MoveToBlockGoal {
    protected final TagKey<Block> blockWithTagToRemove = BlockRegistry.Tags.SCULK_RAID_TARGET_HIGH_PRIORITY;
    protected final SculkCreeperEntity removerMob;
    protected int ticksSinceReachedGoal;
    protected int distanceRequired;
    protected int ticksRequiredToBreakBlock;

    protected int searchCoolDownTicks = TickUnits.convertSecondsToTicks(5);
    protected int searchCoolDownTicksRemaining = 0;

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

        if (!net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.removerMob.level(), this.removerMob))
        {
            return false;
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

    public void stop()
    {
        super.stop();

    }

    public void start() {
        super.start();
    }

    public void playDestroyProgressSound(LevelAccessor level, BlockPos blockPos) {
    }

    public void playBreakSound(Level level, BlockPos blockPos) {
    }

    public void tick() {
        super.tick();
        Level level = this.removerMob.level();
        BlockPos mobPosition = this.removerMob.blockPosition();
        BlockPos blockPosition = this.blockPos;
        RandomSource randomsource = this.removerMob.getRandom();

        if(blockPosition == null)
        {
            return;
        }

        if(!blockPosition.closerThan(mobPosition, distanceRequired))
        {
            ticksSinceReachedGoal = 0;
            return;
        }
        ticksSinceReachedGoal++;



        if (this.ticksSinceReachedGoal > 0)
        {
            Vec3 vec3 = this.removerMob.getDeltaMovement();
            this.removerMob.setDeltaMovement(vec3.x, 0.3D, vec3.z);

            this.removerMob.setSwellDir(1);

            if (!level.isClientSide)
            {
                ((ServerLevel)level).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ItemRegistry.SCULK_MATTER.get())), (double)blockPosition.getX() + 0.5D, (double)blockPosition.getY() + 0.7D, (double)blockPosition.getZ() + 0.5D, 3, ((double)randomsource.nextFloat() - 0.5D) * 0.08D, ((double)randomsource.nextFloat() - 0.5D) * 0.08D, ((double)randomsource.nextFloat() - 0.5D) * 0.08D, (double)0.15F);
            }
        }

        if (this.ticksSinceReachedGoal % 2 == 0)
        {
            Vec3 vec31 = this.removerMob.getDeltaMovement();
            this.removerMob.setDeltaMovement(vec31.x, -0.3D, vec31.z);
            if (this.ticksSinceReachedGoal % 6 == 0) {
                this.playDestroyProgressSound(level, this.blockPos);
            }
        }

        if (this.ticksSinceReachedGoal > ticksRequiredToBreakBlock)
        {
            this.removerMob.explodeSculkCreeper();
            level.destroyBlock(blockPosition, true);

            if (!level.isClientSide)
            {
                for(int i = 0; i < 20; ++i) {
                    double d3 = randomsource.nextGaussian() * 0.02D;
                    double d1 = randomsource.nextGaussian() * 0.02D;
                    double d2 = randomsource.nextGaussian() * 0.02D;
                    ((ServerLevel)level).sendParticles(ParticleTypes.POOF, (double)blockPosition.getX() + 0.5D, (double)blockPosition.getY(), (double)blockPosition.getZ() + 0.5D, 1, d3, d1, d2, (double)0.15F);
                }

                this.playBreakSound(level, blockPosition);
            }
        }
    }

    protected static boolean isBlockEqualOrHigherPriorityThanCurrentTarget(BlockState objectiveBlockState, BlockState blockState)
    {

        // If current target is high priority, and blockState is high priority, return true
        if(objectiveBlockState.is(BlockRegistry.Tags.SCULK_RAID_TARGET_HIGH_PRIORITY) && blockState.is(BlockRegistry.Tags.SCULK_RAID_TARGET_HIGH_PRIORITY))
        {
            return true;
        }
        // If current target is medium and blockState is high, return true
        else if(objectiveBlockState.is(BlockRegistry.Tags.SCULK_RAID_TARGET_MEDIUM_PRIORITY) && blockState.is(BlockRegistry.Tags.SCULK_RAID_TARGET_HIGH_PRIORITY))
        {
            return true;
        }
        // If current target is medium and blockState is medium, return true
        else if(objectiveBlockState.is(BlockRegistry.Tags.SCULK_RAID_TARGET_MEDIUM_PRIORITY) && blockState.is(BlockRegistry.Tags.SCULK_RAID_TARGET_MEDIUM_PRIORITY))
        {
            return true;
        }
        // If current target is low and blockState is high, return true
        else if(objectiveBlockState.is(BlockRegistry.Tags.SCULK_RAID_TARGET_LOW_PRIORITY) && blockState.is(BlockRegistry.Tags.SCULK_RAID_TARGET_HIGH_PRIORITY))
        {
            return true;
        }
        // If current target is low and blockState is medium, return true
        else if(objectiveBlockState.is(BlockRegistry.Tags.SCULK_RAID_TARGET_LOW_PRIORITY) && blockState.is(BlockRegistry.Tags.SCULK_RAID_TARGET_MEDIUM_PRIORITY))
        {
            return true;
        }
        // If current target is low and blockState is low, return true
        else if(objectiveBlockState.is(BlockRegistry.Tags.SCULK_RAID_TARGET_LOW_PRIORITY) && blockState.is(BlockRegistry.Tags.SCULK_RAID_TARGET_LOW_PRIORITY))
        {
            return true;
        }

        return false;
    }

    protected boolean isValidTarget(LevelReader levelReader, BlockState blockState) {
        return isBlockEqualOrHigherPriorityThanCurrentTarget(levelReader.getBlockState(SculkHorde.raidHandler.raidData.getObjectiveLocation()), blockState);
    }

    // New Predicate for isValidTarget
    public final Predicate<BlockState> IS_VALID_TARGET = (blockState) -> {
        return isValidTarget(this.mob.level(), blockState);
    };

    @Override
    protected boolean findNearestBlock() {
        Optional<BlockPos> optionalTargetBlock = BlockAlgorithms.findBlockInCube((ServerLevel) this.mob.level(), this.mob.blockPosition(), IS_VALID_TARGET, 16);
        optionalTargetBlock.ifPresent((blockPos) -> {
            this.blockPos = blockPos;
            SculkHorde.LOGGER.debug("Sculk Creeper New Raid Target: " + this.mob.level().getBlockState(this.blockPos).toString());
        });

        return optionalTargetBlock.isPresent();
    }

    @Override
    protected boolean isValidTarget(LevelReader p_25619_, BlockPos p_25620_) {
        return false;
    }
}

