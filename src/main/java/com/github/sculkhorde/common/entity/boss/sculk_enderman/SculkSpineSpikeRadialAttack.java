package com.github.sculkhorde.common.entity.boss.sculk_enderman;

import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.function.Predicate;

public class SculkSpineSpikeRadialAttack extends MeleeAttackGoal
{
    protected int elapsedAttackDuration = 0;
    protected final int executionCooldown = TickUnits.convertSecondsToTicks(20);
    protected int ticksElapsed = executionCooldown;
    protected Vec3 origin;

    public SculkSpineSpikeRadialAttack(PathfinderMob mob) {
        super(mob, 0.0F, true);
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

        if(!mob.closerThan(mob.getTarget(), 5.0D))
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
        return elapsedAttackDuration <= 30;
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



    @Override
    public void start()
    {
        super.start();
        // TODO Trigger Animation
        origin = new Vec3(mob.getX(), mob.getY() + mob.getEyeHeight(), mob.getZ());
        //Disable mob's movement for 10 seconds
        this.mob.getNavigation().stop();
        // Teleport the enderman away from the mob
        getSculkEnderman().teleportAwayFromEntity(mob.getTarget());
    }

    public int getSpawnHeight(BlockPos startPos)
    {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(startPos.getX(), startPos.getY(), startPos.getZ());
        int iterationsElapsed = 0;
        int iterationsMax = 7;
        while(iterationsElapsed < iterationsMax)
        {

            iterationsElapsed++;

            if(!mob.level().getBlockState(mutablePos).canBeReplaced())
            {
                continue;
            }
            mutablePos.move(0, -1, 0);

        }
        return mutablePos.getY() + 1;
    }

    public void spawnSpikesOnCircumference(int radius, int amount)
    {
        Vec3 origin = new Vec3(mob.getX(), mob.getY() + mob.getEyeHeight(), mob.getZ());
        ArrayList<SculkSpineSpikeAttackEntity> entities = new ArrayList<SculkSpineSpikeAttackEntity>();
        ArrayList<Vec3> possibleSpawns = BlockAlgorithms.getPointsOnCircumferenceVec3(origin, radius, amount);
        for(int i = 0; i < possibleSpawns.size(); i++)
        {
            Vec3 spawnPos = possibleSpawns.get(i);
            SculkSpineSpikeAttackEntity entity = ModEntities.SCULK_SPINE_SPIKE_ATTACK.get().create(mob.level());
            assert entity != null;

            double spawnHeight = getSpawnHeight(BlockPos.containing(spawnPos));
            Vec3 possibleSpawnPosition = new Vec3(spawnPos.x(), spawnHeight, spawnPos.z());
            // If the block below our spawn is solid, spawn the entity
            if(!mob.level().getBlockState(BlockPos.containing(possibleSpawnPosition).below()).canBeReplaced())
            {
                entity.setPos(possibleSpawnPosition.x(), possibleSpawnPosition.y(), possibleSpawnPosition.z());
                entities.add(entity);
                entity.setOwner(mob);
            }
        }

        for (SculkSpineSpikeAttackEntity entity : entities) {
            mob.level().addFreshEntity(entity);
        }
    }

    @Override
    public void tick()
    {
        super.tick();
        if(elapsedAttackDuration == 0)
        {
            spawnSpikesOnCircumference(1, 8);
        }
        if(elapsedAttackDuration == 5)
        {
            spawnSpikesOnCircumference(4, 16);
        }
        else if(elapsedAttackDuration == 10)
        {
            spawnSpikesOnCircumference(5, 5*2);
        }
        else if(elapsedAttackDuration == 15)
        {
            spawnSpikesOnCircumference(6, 6*3);
        }
        else if(elapsedAttackDuration == 20)
        {
            spawnSpikesOnCircumference(8, 8*3);
        }
        else if(elapsedAttackDuration == 25)
        {
            spawnSpikesOnCircumference(10, 10*4);
        }
        else if(elapsedAttackDuration == 30)
        {
            spawnSpikesOnCircumference(12, 12*4);
        }

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
