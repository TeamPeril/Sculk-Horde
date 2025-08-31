package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.goals;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity;
import com.github.sculkhorde.common.entity.goal.AttackStepGoal;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class ReaperCastSpellGoal extends AttackStepGoal {
    public ReaperCastSpellGoal(SculkSoulReaperEntity mob) {
        super(mob);
    }

    SculkSoulReaperEntity getReaper()
    {
        if(mob instanceof SculkSoulReaperEntity reaper)
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
