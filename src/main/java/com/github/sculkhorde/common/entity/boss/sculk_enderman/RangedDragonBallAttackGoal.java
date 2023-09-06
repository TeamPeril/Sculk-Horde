package com.github.sculkhorde.common.entity.boss.sculk_enderman;

import com.github.sculkhorde.util.TickUnits;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.DragonFireball;

import java.util.EnumSet;

public class RangedDragonBallAttackGoal extends Goal
{
    private final Mob mob;
    protected int maxAttackDuration = 0;
    protected int elapsedAttackDuration = 0;
    protected final int executionCooldown = TickUnits.convertSecondsToTicks(5);
    protected int ticksElapsed = executionCooldown;


    public RangedDragonBallAttackGoal(PathfinderMob mob, int durationInTicks) {
        this.mob = mob;
        maxAttackDuration = durationInTicks;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
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

        if(!getSculkEnderman().isSpecialAttackReady() || mob.getTarget() == null)
        {
            return false;
        }

        if(ticksElapsed < executionCooldown)
        {
            return false;
        }

        if(mob.getHealth() < mob.getMaxHealth()/2)
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
        this.mob.getNavigation().stop();
        // Teleport the enderman away from the mob
        getSculkEnderman().teleportAwayFromEntity(mob.getTarget());
        getSculkEnderman().stayInSpecificRangeOfTarget(16, 32);
        getSculkEnderman().triggerAnim("attack_controller", "fireball_shoot_animation");
    }

    @Override
    public void tick()
    {
        super.tick();
        elapsedAttackDuration++;
        if(elapsedAttackDuration == 10)
        {
            performRangedAttack(mob.getTarget());
        }
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
    public void performRangedAttack(LivingEntity targetEntity)
    {

        if(targetEntity == null)
        {
            return;
        }

        double xSpawn = mob.getX();
        double ySpawn = mob.getY() + mob.getEyeHeight();
        double zSpawn = mob.getZ();

        double xDirection = targetEntity.getX() - xSpawn;
        double yDirection = targetEntity.getY(0.5D) - ySpawn;
        double zDirection = targetEntity.getZ() - zSpawn;

        DragonFireball dragonfireball = new DragonFireball(mob.level(), mob, xDirection, yDirection, zDirection);
        dragonfireball.moveTo(xSpawn, ySpawn, zSpawn, 0.0F, 0.0F);
        mob.level().addFreshEntity(dragonfireball);
        //Play blaze shoot sound
        mob.level().playLocalSound(xSpawn, ySpawn, zSpawn, SoundEvents.ENDER_DRAGON_SHOOT, mob.getSoundSource(), 1.0F, 1.0F, false);
    }
}
