package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.SculkEndermanEntity;
import com.github.sculkhorde.common.entity.specialeffects.EnderBubbleAttackEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class EnderBubbleAttackGoal extends MeleeAttackGoal
{
    protected int maxAttackDuration = 0;
    protected int elapsedAttackDuration = 0;
    protected EnderBubbleAttackEntity attackBubble;

    public EnderBubbleAttackGoal(PathfinderMob mob, int durationInTicks) {
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
        if(!getSculkEnderman().isSpecialAttackReady() || mob.getTarget() == null)
        {
            return false;
        }

        if(!mob.closerThan(mob.getTarget(), 3.0D))
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
        // TODO Trigger Animation

        //Disable mob's movement for 10 seconds
        this.mob.getNavigation().stop();

        //Spawn Ender Attack Bubble entity
        attackBubble = new EnderBubbleAttackEntity(this.mob.level(), mob);
        attackBubble.setPos(this.mob.getX(), this.mob.getY() + (this.mob.getBbHeight() / 2), this.mob.getZ());
        this.mob.level().addFreshEntity(attackBubble);
        getSculkEnderman().canTeleport = false;
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
        attackBubble.discard();
        elapsedAttackDuration = 0;
        getSculkEnderman().canTeleport = true;
    }
}
