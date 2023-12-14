package com.github.sculkhorde.common.entity.goal;

import java.util.Comparator;
import java.util.List;

import com.github.sculkhorde.common.entity.SculkRavagerEntity;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;

public class MountNearestRavager extends Goal {
    private final Mob mob; // the skeleton mob
    private Mob target; // the pig to mount
    private int timeToRecalcPath;

    private static final TargetingConditions FIND_RAVAGER_CONTEXT = TargetingConditions.forNonCombat().range(10.0D);
    public MountNearestRavager(Mob mob) {
        this.mob = mob;
    }

    protected boolean isViableTarget(Mob mob) {
        boolean isRavager = mob instanceof SculkRavagerEntity;
        boolean isBeingRidden = mob.hasControllingPassenger();
        boolean isNotThisMob = mob != this.mob;

        return isRavager && !isBeingRidden && isNotThisMob;
    }

    protected Mob getNearestRavager()
    {
        // Use local variables for efficiency
        Class<? extends Mob> mobClass = SculkRavagerEntity.class;
        AABB boundingBox = this.mob.getBoundingBox().inflate(8.0D, 4.0D, 8.0D);

        // Get list of mobs in range
        List<? extends Mob> list = this.mob.level.getEntitiesOfClass(mobClass, boundingBox);

        // Early exit if list is empty
        if (list.isEmpty()) {
            return null;
        }

        for (Mob mob : list) {

        }

        // Use streams to find the closest mob, excluding 'this.mob'
        Mob closestMob = list.stream()
                .filter(this::isViableTarget) // Skip the current mob or if it is already being ridden
                .min(Comparator.comparingDouble(this.mob::distanceToSqr))
                .orElse(null);

        // Check distance condition
        if (closestMob == null) {
            return null;
        }

        return closestMob;
    }

    @Override
    public boolean canUse() {
        // check if the entity is not already riding something
        if (mob.getVehicle() != null) {
            return false;
        }
        // find the nearest ravager within 10 blocks
        Mob possibleRavager = getNearestRavager();

        if(possibleRavager == null) {
            return false;
        }

        target = possibleRavager;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        // continue if the skeleton is not riding the pig and the pig is alive
        return mob.getVehicle() == null && target.isAlive() && !target.hasControllingPassenger();
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        mob.setSprinting(true);
    }

    @Override
    public void tick() {
        // check if the skeleton is close enough to the pig
        if (mob.distanceToSqr(target) < 3.0) {
            // stop the navigation
            mob.getNavigation().stop();
            // mount the pig
            mob.startRiding(target);
        }

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            this.mob.getNavigation().moveTo(this.target, 1.0);
        }
    }

    @Override
    public void stop()
    {
        mob.setSprinting(false);
    }
}