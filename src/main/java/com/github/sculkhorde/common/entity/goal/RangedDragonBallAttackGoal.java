package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.SculkEndermanEntity;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.EnumSet;

public class RangedDragonBallAttackGoal extends Goal
{
    private final Mob mob;
    protected int maxAttackDuration = 0;
    protected int elapsedAttackDuration = 0;
    protected final int executionCooldown = TickUnits.convertSecondsToTicks(10);
    protected int ticksElapsed = executionCooldown;
    private int attackIntervalTicks = TickUnits.convertSecondsToTicks(1);
    private int attackkIntervalCooldown = 0;


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

        if(!mob.closerThan(mob.getTarget(), 10.0F))
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

        // Teleport the enderman away from the mob
        getSculkEnderman().teleportAwayFromEntity(mob.getTarget());
        getSculkEnderman().canTeleport = false;
    }

    @Override
    public void tick()
    {
        super.tick();
        elapsedAttackDuration++;
        performRangedAttack(mob.getTarget());
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

        attackkIntervalCooldown--;


        if(attackkIntervalCooldown > 0)
        {
            return;
        }

        double xSpawn = mob.getX();
        double ySpawn = mob.getY() + mob.getBbHeight() + 1;
        double zSpawn = mob.getX();

        double xDirection = targetEntity.getX() - xSpawn;
        double yDirection = targetEntity.getY(0.5D) - ySpawn;
        double zDirection = targetEntity.getZ() - zSpawn;

        DragonFireball dragonfireball = new DragonFireball(mob.level(), mob, xDirection, yDirection, zDirection);
        dragonfireball.moveTo(xSpawn, ySpawn, zSpawn, 0.0F, 0.0F);
        mob.level().addFreshEntity(dragonfireball);

        attackkIntervalCooldown = attackIntervalTicks;
    }
}
