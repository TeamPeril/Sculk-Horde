package com.github.sculkhorde.common.entity.boss.sculk_enderman;

import java.util.EnumSet;

import com.github.sculkhorde.util.TickUnits;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.DragonFireball;

public class RainDragonBallAttackGoal extends Goal
{
    private final Mob mob;
    protected int maxAttackDuration = 0;
    protected int elapsedAttackDuration = 0;
    protected final int executionCooldown = TickUnits.convertMinutesToTicks(8);
    protected int ticksElapsed = executionCooldown;
    private int attackIntervalTicks = TickUnits.convertSecondsToTicks(2F);
    private int attackkIntervalCooldown = 0;


    public RainDragonBallAttackGoal(PathfinderMob mob, int durationInTicks) {
        this.mob = mob;
        maxAttackDuration = durationInTicks;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public boolean requiresUpdateEveryTick() {
        return true;
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

        if(!mob.closerThan(mob.getTarget(), 10.0F) || !mob.getTarget().isOnGround())
        {
            return false;
        }

        if(mob.getHealth() > mob.getMaxHealth()/2)
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

    @Override
    public void start()
    {
        super.start();
        getSculkEnderman().triggerAnim("attack_controller", "fireball_sky_summon_animation");
        getSculkEnderman().triggerAnim("twitch_controller", "fireball_sky_twitch_animation");
        this.mob.getNavigation().stop();
        // Teleport the enderman away from the mob
        getSculkEnderman().teleportAwayFromEntity(mob.getTarget());
    }

    @Override
    public void tick()
    {
        super.tick();
        elapsedAttackDuration++;
        spawnFallingDragonBallInRandomPosition(5);
        spawnFallingDragonBallInRandomPosition(5);
        spawnFallingDragonBallInRandomPosition(5);
        spawnFallingDragonBallInRandomPosition(5);
        getSculkEnderman().stayInSpecificRangeOfTarget(32, 40);
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


    public void spawnFallingDragonBallInRandomPosition(int range)
    {


        attackkIntervalCooldown--;


        if(attackkIntervalCooldown > 0)
        {
            return;
        }

        if(mob.getTarget() == null)
        {
            return;
        }

        double xSpawn = mob.getTarget().getX() + (mob.getTarget().getRandom().nextInt(range) - ((double) range / 2));
        double ySpawn = mob.getTarget().level.getMaxBuildHeight();
        double zSpawn = mob.getTarget().getZ() + (mob.getTarget().getRandom().nextInt(range) - ((double) range / 2));

        // Spawn going downwards
        double xDirection = 0;
        double yDirection = -3;
        double zDirection = 0;

        DragonFireball dragonfireball = new DragonFireball(mob.level, mob, xDirection, yDirection, zDirection);
        dragonfireball.moveTo(xSpawn, ySpawn, zSpawn, 0.0F, 0.0F);
        mob.level.addFreshEntity(dragonfireball);

        attackkIntervalCooldown = attackIntervalTicks;
    }

}
