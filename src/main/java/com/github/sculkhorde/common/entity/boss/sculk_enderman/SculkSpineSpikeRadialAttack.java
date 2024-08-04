package com.github.sculkhorde.common.entity.boss.sculk_enderman;

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
    protected int DELAY_BEFORE_ATTACK = 30;
    protected int delayRemaining = DELAY_BEFORE_ATTACK;

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

        if(getSculkEnderman().isSpecialAttackOnCooldown() || mob.getTarget() == null)
        {
            return false;
        }

        if(!mob.closerThan(mob.getTarget(), 12.0D) || !mob.getTarget().onGround())
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
        getSculkEnderman().canTeleport = false;
        getSculkEnderman().getNavigation().stop();
        getSculkEnderman().triggerAnim("attack_controller", "spike_radial_animation");
        getSculkEnderman().triggerAnim("twitch_controller", "spike_radial_twitch_animation");
        origin = new Vec3(mob.getX(), mob.getY() + mob.getEyeHeight(), mob.getZ());
        //Disable mob's movement for 10 seconds
        this.mob.getNavigation().stop();
        // Teleport the enderman away from the mob
        getSculkEnderman().teleportAwayFromEntity(mob.getTarget());

        for(int i = 4; i < 20; i++)
        {
            spawnSpikesOnCircumference(getSculkEnderman(), i, i * 4, (((i - 4) * 5)));
        }
    }

    public static void spawnSpikesOnCircumference(SculkEndermanEntity player, int radius, int amount, int delayTicks)
    {
        Vec3 origin = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
        ArrayList<SculkSpineSpikeAttackEntity> entities = new ArrayList<SculkSpineSpikeAttackEntity>();
        ArrayList<Vec3> possibleSpawns = BlockAlgorithms.getPointsOnCircumferenceVec3(origin, radius, amount);
        for(int i = 0; i < possibleSpawns.size(); i++)
        {
            Vec3 spawnPos = possibleSpawns.get(i);
            SculkSpineSpikeAttackEntity entity = new SculkSpineSpikeAttackEntity(player, player.getX(), player.getY(), player.getZ(), delayTicks);

            double spawnHeight = getSpawnHeight(player, BlockPos.containing(spawnPos));
            Vec3 possibleSpawnPosition = new Vec3(spawnPos.x(), spawnHeight, spawnPos.z());
            // If the block below our spawn is solid, spawn the entity
            if(!player.level().getBlockState(BlockPos.containing(possibleSpawnPosition).below()).canBeReplaced())
            {
                entity.setPos(possibleSpawnPosition.x(), possibleSpawnPosition.y(), possibleSpawnPosition.z());
                entities.add(entity);
                entity.setOwner(player);
            }
        }

        for (SculkSpineSpikeAttackEntity entity : entities) {
            player.level().addFreshEntity(entity);
        }
    }

    public static int getSpawnHeight(SculkEndermanEntity player, BlockPos startPos)
    {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(startPos.getX(), startPos.getY(), startPos.getZ());
        int iterationsElapsed = 0;
        int iterationsMax = 7;
        while(iterationsElapsed < iterationsMax)
        {

            iterationsElapsed++;

            if(!player.level().getBlockState(mutablePos).canBeReplaced())
            {
                continue;
            }
            mutablePos.move(0, -1, 0);

        }
        return mutablePos.getY() + 1;
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
