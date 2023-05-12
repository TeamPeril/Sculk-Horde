package com.github.sculkhorde.common.entity.goal;

import javax.annotation.Nullable;

import com.github.sculkhorde.common.entity.SculkCreeperEntity;
import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.core.ItemRegistry;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.phys.Vec3;

public class DestroyPriorityBlockGoal extends MoveToBlockGoal {
    private final TagKey<Block> blockWithTagToRemove = BlockRegistry.Tags.SCULK_RAID_TARGET_HIGH_PRIORITY;
    private final Mob removerMob;
    private int ticksSinceReachedGoal;
    private static final int WAIT_AFTER_BLOCK_FOUND = 20;
    private int distanceRequired;
    private int ticksRequiredToBreakBlock;

    public DestroyPriorityBlockGoal(PathfinderMob pathfinderMob, double p_25842_, int p_25843_, int distanceRequired, int ticksRequiredToBreakBlock) {
        super(pathfinderMob, p_25842_, 24, p_25843_);
        this.removerMob = pathfinderMob;
        this.distanceRequired = distanceRequired;
        this.ticksRequiredToBreakBlock = ticksRequiredToBreakBlock;
    }

    public boolean canUse() {
        if (!net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.removerMob.level, this.removerMob)) {
            return false;
        } else if (this.nextStartTick > 0) {
            --this.nextStartTick;
            return false;
        } else if (this.findNearestBlock()) {
            this.nextStartTick = reducedTickDelay(WAIT_AFTER_BLOCK_FOUND);
            return true;
        } else {
            this.nextStartTick = this.nextStartTick(this.mob);
            return false;
        }
    }

    public void stop() {
        super.stop();
        this.removerMob.fallDistance = 1.0F;
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
        Level level = this.removerMob.level;
        BlockPos blockpos = this.removerMob.blockPosition();
        BlockPos blockpos1 = this.getPosWithBlock(blockpos, level);
        RandomSource randomsource = this.removerMob.getRandom();

        if(blockpos1 == null)
        {
            return;
        }

        if(!blockpos1.closerThan(blockpos, distanceRequired))
        {
            ticksSinceReachedGoal = 0;
            return;
        }
        ticksSinceReachedGoal++;

        if(this.removerMob instanceof SculkCreeperEntity)
        {
            SculkCreeperEntity sculkCreeperEntity = (SculkCreeperEntity)this.removerMob;
            sculkCreeperEntity.setSwellDir(1);
            sculkCreeperEntity.explodeSculkCreeper();
        }


        if (this.ticksSinceReachedGoal > 0) {
            Vec3 vec3 = this.removerMob.getDeltaMovement();
            this.removerMob.setDeltaMovement(vec3.x, 0.3D, vec3.z);
            if (!level.isClientSide) {
                double d0 = 0.08D;
                ((ServerLevel)level).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ItemRegistry.SCULK_MATTER.get())), (double)blockpos1.getX() + 0.5D, (double)blockpos1.getY() + 0.7D, (double)blockpos1.getZ() + 0.5D, 3, ((double)randomsource.nextFloat() - 0.5D) * 0.08D, ((double)randomsource.nextFloat() - 0.5D) * 0.08D, ((double)randomsource.nextFloat() - 0.5D) * 0.08D, (double)0.15F);
            }
        }

        if (this.ticksSinceReachedGoal % 2 == 0) {
            Vec3 vec31 = this.removerMob.getDeltaMovement();
            this.removerMob.setDeltaMovement(vec31.x, -0.3D, vec31.z);
            if (this.ticksSinceReachedGoal % 6 == 0) {
                this.playDestroyProgressSound(level, this.blockPos);
            }
        }

        if (this.ticksSinceReachedGoal > ticksRequiredToBreakBlock)
        {
            level.removeBlock(blockpos1, true);

            if (!level.isClientSide) {
                for(int i = 0; i < 20; ++i) {
                    double d3 = randomsource.nextGaussian() * 0.02D;
                    double d1 = randomsource.nextGaussian() * 0.02D;
                    double d2 = randomsource.nextGaussian() * 0.02D;
                    ((ServerLevel)level).sendParticles(ParticleTypes.POOF, (double)blockpos1.getX() + 0.5D, (double)blockpos1.getY(), (double)blockpos1.getZ() + 0.5D, 1, d3, d1, d2, (double)0.15F);
                }

                this.playBreakSound(level, blockpos1);
            }
        }
    }

    @Nullable
    private BlockPos getPosWithBlock(BlockPos blockPos, BlockGetter blockGetter) {
        if (blockGetter.getBlockState(blockPos).is(this.blockWithTagToRemove)) {
            return blockPos;
        } else {
            BlockPos[] ablockpos = new BlockPos[]{blockPos.below(), blockPos.west(), blockPos.east(), blockPos.north(), blockPos.south(), blockPos.below().below()};

            for(BlockPos blockpos : ablockpos) {
                if (blockGetter.getBlockState(blockpos).is(this.blockWithTagToRemove)) {
                    return blockpos;
                }
            }

            return null;
        }
    }

    @Override
    protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
        ChunkAccess chunkaccess = levelReader.getChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()), ChunkStatus.FULL, false);
        if (chunkaccess == null) {
            return false;
        } else {
            return chunkaccess.getBlockState(blockPos).is(this.blockWithTagToRemove);
        }
    }
}

