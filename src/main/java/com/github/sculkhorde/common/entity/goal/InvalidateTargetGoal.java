package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.systems.squad_system.Squad;
import com.github.sculkhorde.systems.squad_system.SquadSystem;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.common.entity.components.TargetParameters;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.core.BlockPos;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

public class InvalidateTargetGoal extends Goal {

    private final ISculkSmartEntity mob; // We use this to retrieve the mob that is using this goal.
    private UUID lastTargetUUID;
    private long timeOfLastTargetChange;
    private BlockPos ourLastPositionSinceCheck;
    private long UNREACHABLE_TARGET_TIMEOUT = TickUnits.convertMinutesToTicks(1);
    private long lastTimeSincePositionCheck = 0;
    private long POSITION_CHECK_INTERVAL = TickUnits.convertSecondsToTicks(1);

    public InvalidateTargetGoal(ISculkSmartEntity mob)
    {
        super();
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    public Mob getMob()
    {
        return (Mob) this.mob;
    }

    public LivingEntity getTarget()
    {
        return getMob().getTarget();
    }

    /**
     * Checks if we have been targetting the same entity as last check.
     * @return
     */
    private boolean hasTargetChanged()
    {
        if(getTarget() == null)
        {
            return false;
        }

        if(lastTargetUUID == null || !lastTargetUUID.equals(getTarget().getUUID()))
        {
            // If it has changed, update the lastTargetUUID
            lastTargetUUID = getTarget().getUUID();
            timeOfLastTargetChange = getMob().level().getGameTime();
            return true;
        }
        return false;
    }

    private boolean tooCloseToLastPosition()
    {
        return BlockAlgorithms.getBlockDistance(ourLastPositionSinceCheck, getMob().blockPosition()) < 5;
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    @Override
    public boolean canUse()
    {
        if(getTarget() == null)
        {
            return false;
        }

        Optional<Squad> squad = SquadSystem.getSquadOfLivingEntity(getMob());
        if(squad.isPresent()) {

            boolean isLeaderOfSquad = squad.get().isLeader(getMob().getUUID());
            if (!isLeaderOfSquad) {
                return false;
            }
        }

        TargetParameters targetParameters = mob.getTargetParameters();
        if(TickUnits.hasTicksPassed(lastTimeSincePositionCheck, getMob().level(), POSITION_CHECK_INTERVAL))
        {
            ourLastPositionSinceCheck = getMob().blockPosition();
            lastTimeSincePositionCheck = getMob().level().getGameTime();
        }

        // If target has not changed, and we reached threshold, and we are still within 15 blocks of last position, invalidate target.
        //BUG FIX: Forgot to check if the target was a MobEntity before casting it.
        if(targetParameters.canBlackListMobs() && getTarget() instanceof Mob && !hasTargetChanged() && TickUnits.hasTicksPassed(timeOfLastTargetChange, getMob().level(), UNREACHABLE_TARGET_TIMEOUT) && tooCloseToLastPosition())
        {
            targetParameters.addToBlackList((Mob) getTarget());
        }

        boolean result = !targetParameters.isEntityValidSculkHordeTarget(getTarget(), true);
        return result;
    }

    @Override
    public void start()
    {
        getMob().setTarget(null);
        getMob().getTarget();
    }

    @Override
    public boolean canContinueToUse()
    {
        return false;
    }
}
