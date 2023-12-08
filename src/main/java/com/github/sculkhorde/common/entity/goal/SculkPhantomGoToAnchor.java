package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.SculkPhantomEntity;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;

public class SculkPhantomGoToAnchor extends Goal {
    private final SculkPhantomEntity sculkPhantom; // the skeleton mob
    private int timeToRecalcPath;
    private float speedModifier = 1.0F; // Doesn't actually do anything

    private final int IN_RANGE_OF_ANCHOR = 20;
    public SculkPhantomGoToAnchor(SculkPhantomEntity mob) {
        this.sculkPhantom = mob;
    }

    private Mob getMob() {
        return (Mob) this.sculkPhantom;
    }

    public boolean isInRangeOfAnchor()
    {
        return getMob().distanceToSqr(sculkPhantom.getAnchorPoint()) < IN_RANGE_OF_ANCHOR;
    }

    @Override
    public boolean canUse() {

        boolean isAnchorNull = sculkPhantom.getAnchorPoint() == null;

        if(isAnchorNull)
        {
            return false;
        }

        if (isInRangeOfAnchor()) {
            // stop the navigation
            getMob().getNavigation().stop();
            return false;
        }

        if(sculkPhantom.attackPhase == SculkPhantomEntity.AttackPhase.SWOOP)
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void tick() {
        boolean isAnchorNull = sculkPhantom.getAnchorPoint() == null;

        if(isAnchorNull)
        {
            return;
        }

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(TickUnits.convertSecondsToTicks(10));
            Path path = sculkPhantom.getNavigation().createPath(sculkPhantom.getAnchorPoint().x, sculkPhantom.getAnchorPoint().y, sculkPhantom.getAnchorPoint().z, 1);
            this.getMob().getNavigation().moveTo(path, speedModifier);
        }
    }

    @Override
    public void stop() {
        this.getMob().getNavigation().stop();
    }
}
