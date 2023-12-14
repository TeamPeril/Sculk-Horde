package com.github.sculkhorde.common.entity.boss.sculk_enderman;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;

import com.github.sculkhorde.common.entity.boss.SpecialEffectEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class ChaosRiftAttackGoal extends MeleeAttackGoal
{
    protected int maxAttackDuration = 0;
    protected int elapsedAttackDuration = 0;
    protected final int executionCooldown = TickUnits.convertSecondsToTicks(10);
    protected int ticksElapsed = executionCooldown;

    public ChaosRiftAttackGoal(PathfinderMob mob, int durationInTicks) {
        super(mob, 0.0F, true);
        maxAttackDuration = durationInTicks;
    }

    private SculkEndermanEntity getSculkEnderman()
    {
        return (SculkEndermanEntity)this.mob;
    }

    @Override
    public boolean canUse()
    {
        ticksElapsed++;

        if(getSculkEnderman().isSpecialAttackOnCooldown() || mob.getTarget() == null)
        {
            return false;
        }

        if(!mob.closerThan(mob.getTarget(), 5.0D) || !mob.getTarget().isOnGround())
        {
            return false;
        }

        if(ticksElapsed < executionCooldown)
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean canContinueToUse()
    {
        return elapsedAttackDuration < maxAttackDuration;
    }

    private Predicate<BlockPos> isValidSpawn = (pos) -> {

        // If air or water or lava, return false
        if(mob.level.getBlockState(pos).isAir() || mob.level.getBlockState(pos).getFluidState().isSource())
        {
            return false;
        }
        else if(!mob.level.getBlockState(pos.above()).canBeReplaced() || mob.level.getBlockState(pos.above()).getFluidState().isSource())
        {
            return false;
        }
        else if(!mob.level.getBlockState(pos.above().above()).canBeReplaced() || mob.level.getBlockState(pos.above().above()).getFluidState().isSource())
        {
            return false;
        }
        return true;
    };


    @Override
    public void start()
    {
        super.start();
        getSculkEnderman().triggerAnim("attack_controller", "rifts_summon_animation");

        //Disable mob's movement
        this.mob.getNavigation().stop();
        getSculkEnderman().canTeleport = false;
        // Teleport the enderman away from the mob
        getSculkEnderman().teleportAwayFromEntity(mob.getTarget());

        ArrayList<BlockPos> possibleSpawns = BlockAlgorithms.getBlocksInAreaWithBlockPosPredicate((ServerLevel) mob.level, mob.blockPosition(), isValidSpawn, 10);
        // Shuffle
        Collections.shuffle(possibleSpawns);

        // Spawn 10 units
        for(int i = 0; i < 20 && i < possibleSpawns.size(); i++)
        {
            BlockPos spawnPos = possibleSpawns.get(i);
            // Spawn unit
            SpecialEffectEntity entity = ChaosTeleporationRiftEntity.spawn( mob.level, mob, spawnPos.above().above(), ModEntities.CHAOS_TELEPORATION_RIFT.get());
            entity.setOwner(mob);
        }
    }

    @Override
    public void tick()
    {
        super.tick();
        elapsedAttackDuration++;
    }

    @Override
    public void stop()
    {
        super.stop();
        getSculkEnderman().resetSpecialAttackCooldown();
        elapsedAttackDuration = 0;
        ticksElapsed = 0;
        getSculkEnderman().canTeleport = true;
    }
}
