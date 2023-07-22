package com.github.sculkhorde.common.entity.boss.sculk_enderman;

import com.github.sculkhorde.common.entity.SculkMiteEntity;
import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;

public class SculkSpineSpikeRadialAttack extends MeleeAttackGoal
{
    protected int maxAttackDuration = 0;
    protected int elapsedAttackDuration = 0;
    protected final int executionCooldown = TickUnits.convertSecondsToTicks(20);
    protected int ticksElapsed = executionCooldown;

    public SculkSpineSpikeRadialAttack(PathfinderMob mob, int durationInTicks) {
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
        return elapsedAttackDuration < maxAttackDuration && false;
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

    private ArrayList<Vec3> getPositionsOnCircumferenceOfCircle(int radiusOfCircle, int numberOfPositionsToCreate)
    {
        ArrayList<Vec3> positions = new ArrayList<Vec3>();
        float angleIncrement = (float) (2 * Math.PI / numberOfPositionsToCreate);
        for(int i = 0; i < numberOfPositionsToCreate; i++)
        {
            float angle = i * angleIncrement;
            double x = radiusOfCircle * Math.cos(angle);
            double z = radiusOfCircle * Math.sin(angle);
            positions.add(new Vec3(mob.getX() + x, mob.getY(), mob.getZ() + z));
        }
        return positions;
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
        ArrayList<Vec3> possibleSpawns = getPositionsOnCircumferenceOfCircle(1, 8);
        possibleSpawns.addAll(getPositionsOnCircumferenceOfCircle(4, 16));
        possibleSpawns.addAll(getPositionsOnCircumferenceOfCircle(5, 24));
        possibleSpawns.addAll(getPositionsOnCircumferenceOfCircle(6, 32));
        possibleSpawns.addAll(getPositionsOnCircumferenceOfCircle(8, 40));
        possibleSpawns.addAll(getPositionsOnCircumferenceOfCircle(10, 64));
        possibleSpawns.addAll(getPositionsOnCircumferenceOfCircle(12, 72));

        // Spawn 20 units
        for(int i = 0; i < possibleSpawns.size(); i++)
        {
            Vec3 spawnPos = possibleSpawns.get(i);
            SculkSpineSpikeAttackEntity mite = new SculkSpineSpikeAttackEntity(this.mob, spawnPos.x(), spawnPos.y(), spawnPos.z());
            // Rotate random degree between 0 amd 360
            mite.setYRot((float) (Math.random() * 360));
            mob.level().addFreshEntity(mite);
        }
    }



    @Override
    public void tick()
    {
        super.tick();
        elapsedAttackDuration++;
        //getSculkEnderman().stayInSpecificRangeOfTarget(16, 32);
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
