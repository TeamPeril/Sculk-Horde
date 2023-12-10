package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.SculkPhantomEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class SculkPhantomWanderGoal extends Goal {

    // Constants
    public static final int DEFAULT_INTERVAL = 120;
    private static final double MAX_RANDOM_DISTANCE = 10.0;

    // Fields
    private final SculkPhantomEntity mob;
    private final double speedModifier;
    private long intervalTicks;
    private long lastTimeExecuted;

    private Vec3 targetPosition;
    private boolean forceTrigger;

    protected int maxHeightOffGround;

    // Constructor

    public SculkPhantomWanderGoal(SculkPhantomEntity mob, double speedModifier, long interval, int maxHeightOffGround) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.intervalTicks = interval;
        this.maxHeightOffGround = maxHeightOffGround;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    // Accessors and Mutators

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

        if(mob.getTarget() != null)
        {
            return false;
        }

        if(mob.isScouter())
        {
            return false;
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

            navigateToTarget();

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

    public static Vec3 getGroundPos(Level level, Vec3 origin)
    {
        // Shoot ray cast downward to find ground
        ClipContext context = new ClipContext(origin, origin.add(0, level.getMaxBuildHeight() * -1, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, null);
        BlockHitResult optional = level.clip(context);
        if(optional.getType() == BlockHitResult.Type.MISS)
        {
            return origin;
        }

        return optional.getLocation();
    }

    private Vec3 getRandomPosition() {
        RandomSource random = mob.getRandom();
        double angle = random.nextDouble() * 2.0 * Math.PI;
        double distance = random.nextDouble() * MAX_RANDOM_DISTANCE;

        double dx = Math.cos(angle) * distance;
        double dy = random.nextDouble() * MAX_RANDOM_DISTANCE;
        double dz = Math.sin(angle) * distance;

        Vec3 currentPosition = mob.position();
        Vec3 targetPosition = currentPosition.add(dx, dy, dz);
        Vec3 groundPosition = getGroundPos(mob.level(), targetPosition);

        if(groundPosition.distanceTo(currentPosition) > maxHeightOffGround)
        {
            return groundPosition.add(0, maxHeightOffGround, 0);
        }

        return targetPosition;
    }
}