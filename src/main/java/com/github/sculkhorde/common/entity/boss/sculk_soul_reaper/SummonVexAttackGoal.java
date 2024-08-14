package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper;

import com.github.sculkhorde.common.entity.SculkVexEntity;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumSet;

public class SummonVexAttackGoal extends Goal
{
    private final Mob mob;
    protected final int executionCooldown = TickUnits.convertSecondsToTicks(30);
    protected long lastTimeOfExecution;


    public SummonVexAttackGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private SculkSoulReaperEntity getEntity()
    {
        return (SculkSoulReaperEntity)this.mob;
    }

    @Override
    public boolean canUse()
    {

        if(mob.level().getGameTime() - lastTimeOfExecution < executionCooldown)
        {
            return false;
        }
        if(mob.getTarget() == null)
        {
            return false;
        }


        return true;
    }

    @Override
    public boolean canContinueToUse()
    {
        return false;
    }

    @Override
    public void start()
    {
        super.start();
        performSpellCasting();
        lastTimeOfExecution = mob.level().getGameTime();
    }

    // Performs the spell casting action
    protected void performSpellCasting() {

        for(int i = 0; i < 10; i++)
        {
            SculkVexEntity entity = new SculkVexEntity(mob.level());
            entity.setPos(mob.getX(), mob.getY() + 1, mob.getZ());
            entity.finalizeSpawn((ServerLevelAccessor) mob.level(), mob.level().getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.MOB_SUMMONED, (SpawnGroupData)null, (CompoundTag)null);
            entity.setOwner(mob);
            entity.setBoundOrigin(mob.blockPosition());
            entity.setLimitedLife(TickUnits.convertMinutesToTicks(5));
            mob.level().addFreshEntity(entity);
        }
    }

    // Creates a spell entity at the specified coordinates
    private void createSpellEntity(double x, double z, double minY, double maxY, float angle, int delay) {
        BlockPos blockPos = BlockPos.containing(x, maxY, z);
        boolean foundSuitablePosition = false;
        double yOffset = 0.0D;

        // Find a suitable position for the spell entity
        do {
            BlockPos belowBlockPos = blockPos.below();
            BlockState belowBlockState = mob.level().getBlockState(belowBlockPos);
            if (belowBlockState.isFaceSturdy(mob.level(), belowBlockPos, Direction.UP)) {
                if (!mob.level().isEmptyBlock(blockPos)) {
                    BlockState blockState = mob.level().getBlockState(blockPos);
                    VoxelShape voxelShape = blockState.getCollisionShape(mob.level(), blockPos);
                    if (!voxelShape.isEmpty()) {
                        yOffset = voxelShape.max(Direction.Axis.Y);
                    }
                }

                foundSuitablePosition = true;
                break;
            }

            blockPos = blockPos.below();
        } while (blockPos.getY() >= Mth.floor(minY) - 1);

        // Add the spell entity to the world if a suitable position is found
        if (foundSuitablePosition) {
            mob.level().addFreshEntity(new EvokerFangs(mob.level(), x, (double)blockPos.getY() + yOffset, z, angle, delay, mob));
        }
    }

}
