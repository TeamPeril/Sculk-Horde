package com.github.sculkhorde.common.entity.boss.sculk_enderman;

import java.util.ArrayList;
import java.util.function.Predicate;

import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.phys.Vec3;

public class SculkSpineSpikeLineAttack extends MeleeAttackGoal
{
    protected int elapsedAttackDuration = 0;
    protected final int executionCooldown = TickUnits.convertSecondsToTicks(20);
    protected int ticksElapsed = executionCooldown;
    protected Vec3 origin;
    protected Vec3 lookVec;
    protected int DELAY_BEFORE_ATTACK = 30;
    protected int delayRemaining = DELAY_BEFORE_ATTACK;


    public SculkSpineSpikeLineAttack(PathfinderMob mob) {
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

        if(getSculkEnderman().isSpecialAttackOnCooldown() || mob.getTarget() == null || !ModConfig.SERVER.experimental_features_enabled.get())
        {
            return false;
        }

        if(!mob.closerThan(mob.getTarget(), 13.0D) || !mob.getTarget().isOnGround())
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
        return elapsedAttackDuration <= 60;
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
        getSculkEnderman().canTeleport = false;
        getSculkEnderman().getNavigation().stop();
        getSculkEnderman().triggerAnim("attack_controller", "spike_radial_animation");
        getSculkEnderman().triggerAnim("twitch_controller", "spike_radial_twitch_animation");
        origin = new Vec3(mob.getX(), mob.getY() + mob.getEyeHeight(), mob.getZ());
        lookVec = mob.getLookAngle();
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

            if(!mob.level.getBlockState(mutablePos).canBeReplaced())
            {
                continue;
            }
            mutablePos.move(0, -1, 0);

        }
        return mutablePos.getY() + 1;
    }

    public void spawnSpikesOnCircumference(int radius, int amount)
    {
        ArrayList<SculkSpineSpikeAttackEntity> entities = new ArrayList<SculkSpineSpikeAttackEntity>();
        ArrayList<Vec3> possibleSpawns = BlockAlgorithms.getPointsOnCircumferenceVec3(origin, radius, amount);
        for(int i = 0; i < possibleSpawns.size(); i++)
        {
            Vec3 spawnPos = possibleSpawns.get(i);
            SculkSpineSpikeAttackEntity entity = ModEntities.SCULK_SPINE_SPIKE_ATTACK.get().create(mob.level);
            assert entity != null;

            double spawnHeight = getSpawnHeight(BlockPos.containing(spawnPos));
            Vec3 possibleSpawnPosition = new Vec3(spawnPos.x(), spawnHeight, spawnPos.z());
            // If the block below our spawn is solid, spawn the entity
            if(!mob.level.getBlockState(BlockPos.containing(possibleSpawnPosition).below()).canBeReplaced())
            {
                entity.setPos(possibleSpawnPosition.x(), possibleSpawnPosition.y(), possibleSpawnPosition.z());
                entities.add(entity);
                entity.setOwner(mob);
            }
        }

        for (SculkSpineSpikeAttackEntity entity : entities) {
            mob.level.addFreshEntity(entity);
        }
    }

    public void spawnSpikesInPerpendicularLineInFrontOfEnderman(int offsetFromEnderman)
    {
        Vec3 middle = origin.add(lookVec.scale(offsetFromEnderman));

        // Spawn spikes to the left and to the right of this spike, perpendicular to the enderman's look vector
        Vec3 left = new Vec3(origin.z(), 0, -origin.x());
        Vec3 right = new Vec3(-origin.z(), 0, origin.x());

        left = new Vec3(middle.x() + left.x(), getSpawnHeight(BlockPos.containing(middle)), middle.z() + left.z());
        right = new Vec3(middle.x() + right.x(), getSpawnHeight(BlockPos.containing(middle)), middle.z() + right.z());


        // Spawn left, right, and middle entity
        SculkSpineSpikeAttackEntity leftEntity = ModEntities.SCULK_SPINE_SPIKE_ATTACK.get().create(mob.level);
        assert leftEntity != null;
        leftEntity.setPos(left.x(), left.y(), left.z());
        leftEntity.setOwner(mob);
        mob.level.addFreshEntity(leftEntity);

        SculkSpineSpikeAttackEntity rightEntity = ModEntities.SCULK_SPINE_SPIKE_ATTACK.get().create(mob.level);
        assert rightEntity != null;
        rightEntity.setPos(right.x(), right.y(),  right.z());
        rightEntity.setOwner(mob);
        mob.level.addFreshEntity(rightEntity);

        SculkSpineSpikeAttackEntity middleEntity = ModEntities.SCULK_SPINE_SPIKE_ATTACK.get().create(mob.level);
        assert middleEntity != null;
        middleEntity.setPos(middle.x(), middle.y(), middle.z());
        middleEntity.setOwner(mob);
        mob.level.addFreshEntity(middleEntity);


    }

    @Override
    public void tick()
    {
        super.tick();

        delayRemaining = Math.max(0, delayRemaining - 1);

        if(delayRemaining != 0)
        {
            return;
        }

        spawnSpikesInPerpendicularLineInFrontOfEnderman(elapsedAttackDuration + 1);
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
        delayRemaining = DELAY_BEFORE_ATTACK;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
