package com.github.sculkhorde.common.entity.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Random;

public class ImprovedFlyingWanderingGoal extends Goal {

    // Constants
    public static final int DEFAULT_INTERVAL = 120;
    private static final double MAX_RANDOM_DISTANCE = 10.0;

    // Fields
    private final Mob mob;
    private final double speedModifier;
    private long intervalTicks;
    private long lastTimeExecuted;

    private Vec3 targetPosition;
    private boolean forceTrigger;

    // Constructor
    public ImprovedFlyingWanderingGoal(PathfinderMob mob, double speedModifier) {
        this(mob, speedModifier, DEFAULT_INTERVAL);
    }

    public ImprovedFlyingWanderingGoal(Mob mob, double speedModifier, long interval) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.intervalTicks = interval;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    // Accessors and Mutators
    public void setIntervalTicks(int interval) {
        this.intervalTicks = interval;
    }

    // Goal Methods
    @Override
    public boolean canUse() {
        if (mob.isVehicle()) {
            return false;
        } else if (!forceTrigger) {
            if (mob.level().getGameTime() - lastTimeExecuted < intervalTicks) {
                return false;
            }
        }

        targetPosition = getRandomPosition();
        if (targetPosition == null) {
            return false;
        }

        forceTrigger = false;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !mob.getNavigation().isDone() && !mob.isVehicle();
    }

    @Override
    public void start() {
        lastTimeExecuted = mob.level().getGameTime();
        navigateToTarget();
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        super.stop();
    }

    @Override
    public void tick() {
        if (targetPosition != null && !mob.getNavigation().isDone() && !mob.isVehicle()) {
            Vec3 delta = targetPosition.subtract(mob.position());
            if (delta.lengthSqr() < 1.0) {
                navigateToTarget();
            }
        }
    }

    public void trigger() {
        forceTrigger = true;
    }

    // Helper Methods
    private void navigateToTarget() {
        Path path = mob.getNavigation().createPath(BlockPos.containing(targetPosition), 1);
        if (path != null) {
            mob.getNavigation().moveTo(path, speedModifier);
        }
    }

    private Vec3 getRandomPosition() {
        RandomSource random = mob.getRandom();
        double angle = random.nextDouble() * 2.0 * Math.PI;
        double distance = random.nextDouble() * MAX_RANDOM_DISTANCE;

        double dx = Math.cos(angle) * distance;
        double dy = random.nextDouble() * MAX_RANDOM_DISTANCE;
        double dz = Math.sin(angle) * distance;

        Vec3 currentPosition = mob.position();
        return currentPosition.add(dx, dy, dz);
    }
}