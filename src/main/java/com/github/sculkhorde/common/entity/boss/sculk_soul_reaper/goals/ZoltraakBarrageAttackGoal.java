package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.goals;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ZoltraakBarrageAttackGoal extends Goal
{
    private final Mob mob;
    protected int maxAttackDuration = 0;
    protected int elapsedAttackDuration = 0;
    protected final int executionCooldown = TickUnits.convertSecondsToTicks(10);
    protected int ticksElapsed = executionCooldown;
    protected int attackIntervalTicks = TickUnits.convertSecondsToTicks(0.5F);
    protected int attackkIntervalCooldown = 0;


    public ZoltraakBarrageAttackGoal(PathfinderMob mob, int durationInTicks) {
        this.mob = mob;
        maxAttackDuration = durationInTicks;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse()
    {
        ticksElapsed++;

        if(mob.getTarget() == null)
        {
            return false;
        }

        if(ticksElapsed < executionCooldown)
        {
            return false;
        }

        if(!mob.getSensing().hasLineOfSight(mob.getTarget()))
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

    protected BlockPos getRandomBlockPosAboveEntity()
    {
        int xOffset = mob.getRandom().nextInt(-2, 2);
        int yOffset = mob.getRandom().nextInt(-2, 2);
        int zOffset = mob.getRandom().nextInt(-2, 2);

        return new BlockPos(mob.getBlockX() + xOffset, mob.getBlockY() + 5 + yOffset, mob.getBlockZ() + zOffset);
    }

    @Override
    public void start()
    {
        super.start();
        //getEntity().triggerAnim("attack_controller", "fireball_sky_summon_animation");
        //getEntity().triggerAnim("twitch_controller", "fireball_sky_twitch_animation");
    }

    @Override
    public void tick()
    {
        super.tick();
        elapsedAttackDuration++;
        spawnSoulAndShootAtTarget(5);
    }

    @Override
    public void stop()
    {
        super.stop();
        elapsedAttackDuration = 0;
        ticksElapsed = 0;
    }


    public void spawnSoulAndShootAtTarget(int range)
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

        SculkSoulReaperEntity.shootZoltraakBeam(getRandomBlockPosAboveEntity().getCenter(), mob, mob.getTarget(), 8F, 0.3F, 10);

        attackkIntervalCooldown = attackIntervalTicks;
    }

}
//projectileEntity.shoot(d0, d1 + d3 * (double)0.2F, d2, 1.6F, (float)(14 - mob.level().getDifficulty().getId() * 4));