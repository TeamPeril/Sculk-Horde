package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.SculkPhantomEntity;
import com.github.sculkhorde.core.ModConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;

public class SculkPhantomGoToAnchor extends Goal {
    private final SculkPhantomEntity sculkPhantom; // the skeleton mob
    private int timeToRecalcPath;

    private final int FOLLOW_RANGE = 20;

    private float speedModifier = 1.0F;

    public SculkPhantomGoToAnchor(SculkPhantomEntity mob, float speedModifier) {
        this.sculkPhantom = mob;
    }

    private Mob getMob() {
        return (Mob) this.sculkPhantom;
    }

    @Override
    public boolean canUse() {

        boolean isAnchorNull = sculkPhantom.getAnchorPoint() == null;

        if(isAnchorNull)
        {
            return false;
        }

        return true;
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

        if (getMob().distanceToSqr(sculkPhantom.getAnchorPoint()) < FOLLOW_RANGE) {
            // stop the navigation
            getMob().getNavigation().stop();

        }

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(20);
            Path path =sculkPhantom.getNavigation().createPath(sculkPhantom.getAnchorPoint().x, sculkPhantom.getAnchorPoint().y, sculkPhantom.getAnchorPoint().z, 1);
            this.getMob().getNavigation().moveTo(path, speedModifier);
        }
    }
}
