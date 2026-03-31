package com.github.sculkhorde.common.entity.boss.angel_of_reaping.goals;

import com.github.sculkhorde.common.entity.boss.angel_of_reaping.AngelOfReapingEntity;
import com.github.sculkhorde.common.entity.goal.AttackStepGoal;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class ReaperCastSpellGoal extends AttackStepGoal {
    public ReaperCastSpellGoal(AngelOfReapingEntity mob) {
        super(mob);
    }

    AngelOfReapingEntity getReaper()
    {
        if(mob instanceof AngelOfReapingEntity reaper)
        {
            return reaper;
        }

        return null;
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start()
    {
        super.start();

        if(mob.level().isClientSide())
        {
            return;
        }

        mob.level().playSound(mob, mob.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.0F, 1.0F);
    }

}
