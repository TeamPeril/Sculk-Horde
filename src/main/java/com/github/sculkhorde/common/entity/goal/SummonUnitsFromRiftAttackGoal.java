package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.SculkEndermanEntity;
import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactory;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactoryEntry;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;

public class SummonUnitsFromRiftAttackGoal extends MeleeAttackGoal
{
    protected int maxAttackDuration = 0;
    protected int elapsedAttackDuration = 0;
    protected final int executionCooldown = TickUnits.convertSecondsToTicks(20);
    protected int ticksElapsed = executionCooldown;

    public SummonUnitsFromRiftAttackGoal(PathfinderMob mob, int durationInTicks) {
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

        if(!getSculkEnderman().isSpecialAttackReady() || mob.getTarget() == null)
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

    private Predicate<EntityFactoryEntry> isValidReinforcement()
    {
        return (entityFactoryEntry) ->
        {
            if(entityFactoryEntry.getEntity() == EntityRegistry.SCULK_CREEPER.get() || entityFactoryEntry.getEntity() == EntityRegistry.SCULK_RAVAGER.get())
            {
                return false;
            }

            return entityFactoryEntry.getCategory() == EntityFactory.StrategicValues.Melee || entityFactoryEntry.getCategory() == EntityFactory.StrategicValues.Ranged;
        };
    }

    @Override
    public void start()
    {
        super.start();
        // TODO Trigger Animation

        //Disable mob's movement for 10 seconds
        this.mob.getNavigation().stop();
        // Teleport the enderman away from the mob
        getSculkEnderman().teleportAwayFromEntity(mob.getTarget());
        getSculkEnderman().canTeleport = false;
        ArrayList<BlockPos> possibleSpawns = BlockAlgorithms.getBlocksInAreaWithBlockPosPredicate((ServerLevel) mob.level, mob.blockPosition(), isValidSpawn, 5);
        // Shuffle
        Collections.shuffle(possibleSpawns);

        // Spawn 10 units
        for(int i = 0; i < 10 && i < possibleSpawns.size(); i++)
        {
            BlockPos spawnPos = possibleSpawns.get(i);
            // Spawn unit
            Optional<EntityFactoryEntry> entry =  SculkHorde.entityFactory.getRandomEntry(isValidReinforcement());

            if(entry.isPresent())
            {
                entry.get().spawnEntity((ServerLevel) mob.level, spawnPos.above());
            }
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