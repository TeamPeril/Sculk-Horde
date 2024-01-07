package com.github.sculkhorde.common.entity.boss.sculk_enderman;

import com.github.sculkhorde.core.ModEntities;
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

public class SummonRandomAttackUnits extends MeleeAttackGoal
{
    protected int maxAttackDuration = 0;
    protected int elapsedAttackDuration = 0;
    protected final int executionCooldown = TickUnits.convertSecondsToTicks(40);
    protected int ticksElapsed = executionCooldown;

    protected EntityFactoryEntry.StrategicValues[] validStrategicValues = new EntityFactoryEntry.StrategicValues[]{EntityFactoryEntry.StrategicValues.Combat};

    public SummonRandomAttackUnits(PathfinderMob mob, int durationInTicks) {
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
        if(mob.level().getBlockState(pos).isAir() || mob.level().getBlockState(pos).getFluidState().isSource())
        {
            return false;
        }
        else if(!mob.level().getBlockState(pos.above()).canBeReplaced() || mob.level().getBlockState(pos.above()).getFluidState().isSource())
        {
            return false;
        }
        else if(!mob.level().getBlockState(pos.above().above()).canBeReplaced() || mob.level().getBlockState(pos.above().above()).getFluidState().isSource())
        {
            return false;
        }
        return true;
    };

    private Predicate<EntityFactoryEntry> isValidReinforcement()
    {
        return (entityFactoryEntry) ->
        {
            return entityFactoryEntry.doesEntityContainAnyRequiredStrategicValues(validStrategicValues);
        };
    }

    @Override
    public void start()
    {
        super.start();
        //getSculkEnderman().triggerAnim("attack_controller", "summon_animation");
        //getSculkEnderman().triggerAnim("twitch_controller", "summon_twitch_animation");

        //Disable mob's movement
        this.mob.getNavigation().stop();
        // Teleport the enderman away from the mob
        getSculkEnderman().teleportAwayFromEntity(mob.getTarget());
        ArrayList<BlockPos> possibleSpawns = BlockAlgorithms.getBlocksInAreaWithBlockPosPredicate((ServerLevel) mob.level(), mob.blockPosition(), isValidSpawn, 5);
        // Shuffle
        Collections.shuffle(possibleSpawns);

        // Spawn 10 units
        for(int i = 0; i < 10 && i < possibleSpawns.size(); i++)
        {
            BlockPos spawnPos = possibleSpawns.get(i);
            // Spawn unit
            Optional<EntityFactoryEntry> entry =  EntityFactory.getRandomEntry(isValidReinforcement());

            if(entry.isPresent())
            {
                entry.get().spawnEntity((ServerLevel) mob.level(), spawnPos.above());
            }
        }

    }

    @Override
    public void tick()
    {
        super.tick();
        elapsedAttackDuration++;
        getSculkEnderman().stayInSpecificRangeOfTarget(16, 32);
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
