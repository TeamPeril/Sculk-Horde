package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.SculkGhastEntity;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

public class SculkGhastDeployTroopsAtGoalPosition extends Goal {
    protected final SculkGhastEntity mob;
    protected final int MIN_DISTANCE_TO_GOAL_POS = 5;
    protected int timeToRecalcPath;
    protected long timeOfLastPathRecalculation = 0;
    protected float pathRecalculationCooldown;
    protected boolean isTimeToResetPathCooldown = true;


    protected long timeOfLastMobRelease = 0;
    protected final long TICKS_BETWEEN_MOB_DEPLOYMENT = TickUnits.convertSecondsToTicks(2);
    protected final long MAX_DEPLOYED_MOBS = 20;
    protected long deployedMobs = 0;
    protected Path pathToGoalPos;
    protected boolean finishedDeployment = false;
    public SculkGhastDeployTroopsAtGoalPosition(SculkGhastEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    protected Mob getMob() {
        return (Mob) this.mob;
    }

    public boolean isInRangeOfAnchor()
    {
        if(mob.getGoalPos() == null)
        {
            return false;
        }

        return BlockAlgorithms.getDistance(mob.blockPosition().getCenter(), mob.getGoalPos()) < MIN_DISTANCE_TO_GOAL_POS;
    }

    @Override
    public boolean canUse() {

        boolean isGoalPosNull = mob.getGoalPos() == null;

        if(isGoalPosNull)
        {
            return false;
        }

        if (!mob.isAnchorPosValid(BlockPos.containing(mob.getGoalPos()))) {
            return false;
        }

        if(finishedDeployment)
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
        boolean isAnchorNull = mob.getGoalPos() == null;

        if(isAnchorNull)
        {
            return;
        }

        float distanceFromGoalPos = BlockAlgorithms.getDistance(mob.position(), mob.getGoalPos());

        if(!isInRangeOfAnchor())
        {
            if(isTimeToResetPathCooldown)
            {
                if(distanceFromGoalPos <= 5)
                {
                    pathRecalculationCooldown = TickUnits.convertSecondsToTicks(0.5F);
                }
                else if(distanceFromGoalPos <= 10)
                {
                    pathRecalculationCooldown = TickUnits.convertSecondsToTicks(1F);
                }
                else if(distanceFromGoalPos <= 32)
                {
                    pathRecalculationCooldown = TickUnits.convertSecondsToTicks(3F);
                }
                else
                {
                    pathRecalculationCooldown = TickUnits.convertSecondsToTicks(6F);
                }
                isTimeToResetPathCooldown = false;
            }


            if(mob.level().getGameTime() - timeOfLastPathRecalculation >= pathRecalculationCooldown || pathToGoalPos == null)
            {
                pathToGoalPos = mob.getNavigation().createPath(mob.getGoalPos().x, mob.getGoalPos().y, mob.getGoalPos().z, 1);
                this.getMob().getNavigation().moveTo(pathToGoalPos, 1.0F);
                timeOfLastPathRecalculation = mob.level().getGameTime();
                isTimeToResetPathCooldown = true;
            }
            return;
        }



        // Once we are in range, start deploying troops.
        if(mob.level().getGameTime() - timeOfLastMobRelease < TICKS_BETWEEN_MOB_DEPLOYMENT)
        {
            mob.getNavigation().stop();
            return;
        }

        mob.releaseMob();
        deployedMobs++;

        if(deployedMobs >= MAX_DEPLOYED_MOBS)
        {
            finishedDeployment = true;
        }
    }

    @Override
    public void stop() {
        this.getMob().getNavigation().stop();
    }
}
