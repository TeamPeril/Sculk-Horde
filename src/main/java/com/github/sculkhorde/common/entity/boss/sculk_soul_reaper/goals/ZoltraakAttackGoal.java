package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.goals;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;

import static com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity.shootZoltraakBeam;

public class ZoltraakAttackGoal extends Goal
{
    private final SculkSoulReaperEntity mob;
    protected final int executionCooldown = TickUnits.convertSecondsToTicks(5);
    protected int ticksElapsed = executionCooldown;

    protected final int baseCastingTime = TickUnits.convertSecondsToTicks(1);
    protected int castingTime = 0;
    protected boolean spellCasted = false;
    protected float DAMAGE = 8F;
    protected int minDifficulty = 0;
    protected int maxDifficulty = 0;


    public ZoltraakAttackGoal(SculkSoulReaperEntity mob, int minDifficulty, int maxDifficulty) {
        this.mob = mob;
        this.minDifficulty = minDifficulty;
        this.maxDifficulty = maxDifficulty;
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }


    protected int getCastingTimeElapsed()
    {
        return castingTime;
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

        if(mob.getMobDifficultyLevel() < minDifficulty)
        {
            return false;
        }

        if(mob.getMobDifficultyLevel() > maxDifficulty && maxDifficulty != -1)
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean canContinueToUse()
    {
        return !spellCasted && mob.getTarget() != null;
    }

    @Override
    public void start()
    {
        super.start();

        if(mob.level().isClientSide())
        {
            return;
        }

        mob.level().playSound(mob,mob.blockPosition(), SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 1.0F, 1.0F);

        //getEntity().triggerAnim("attack_controller", "fireball_sky_summon_animation");
        //getEntity().triggerAnim("twitch_controller", "fireball_sky_twitch_animation");
    }

    @Override
    public void tick()
    {
        super.tick();

        if(mob.level().isClientSide())
        {
            return;
        }

        if(getCastingTimeElapsed() < baseCastingTime)
        {
            castingTime++;
            return;
        }

        if(spellCasted)
        {
            return;
        }

        shootZoltraakBeam(mob.getEyePosition(), mob, mob.getTarget(), DAMAGE, 0.3F, 10F);
        spellCasted = true;
    }

    @Override
    public void stop()
    {
        super.stop();
        ticksElapsed = 0;
        spellCasted = false;
        castingTime = 0;
    }

    public double getRandomDoubleInRange(double min, double max)
    {
        return min + (mob.getRandom().nextFloat() * (max + min));
    }
    public int getRandomIntInRange(int min, int max)
    {
        return min + (mob.getRandom().nextInt() * (max + min));
    }



}