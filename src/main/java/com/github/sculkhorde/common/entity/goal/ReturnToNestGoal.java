package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.blockentity.BroodNestBlockEntity;
import com.github.sculkhorde.common.entity.SculkBroodHatcherEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.EnumSet;

public class ReturnToNestGoal extends Goal {

    private final SculkBroodHatcherEntity mob;
    private BlockPos nestPos;
    private final double speedModifier;
    private final float distanceToEnter = 2.0F;

    public ReturnToNestGoal(SculkBroodHatcherEntity mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!mob.isIdle()) {
            return false;
        }

        CompoundTag data = mob.getPersistentData();
        if (!data.contains("nestX") || !data.contains("nestY") || !data.contains("nestZ")) {
            return false;
        }

        nestPos = new BlockPos(data.getInt("nestX"), data.getInt("nestY"), data.getInt("nestZ"));

        BlockEntity tile = mob.level().getBlockEntity(nestPos);
        if (!(tile instanceof BroodNestBlockEntity)) {
            return false;
        }

        return mob.distanceToSqr(nestPos.getX() + 0.5D, nestPos.getY(), nestPos.getZ() + 0.5D) > distanceToEnter * distanceToEnter;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(nestPos.getX() + 0.5D, nestPos.getY(), nestPos.getZ() + 0.5D, speedModifier);
    }

    @Override
    public void tick() {
        if (nestPos == null) return;

        if (mob.distanceToSqr(nestPos.getX() + 0.5D, nestPos.getY(), nestPos.getZ() + 0.5D) <= distanceToEnter * distanceToEnter) {
            BlockEntity tile = mob.level().getBlockEntity(nestPos);
            if (tile instanceof BroodNestBlockEntity nest) {
                nest.occupyNest(mob);
            }
        } else if (mob.getNavigation().isDone()) {
             this.mob.getNavigation().moveTo(nestPos.getX() + 0.5D, nestPos.getY(), nestPos.getZ() + 0.5D, speedModifier);
        }
    }
}
