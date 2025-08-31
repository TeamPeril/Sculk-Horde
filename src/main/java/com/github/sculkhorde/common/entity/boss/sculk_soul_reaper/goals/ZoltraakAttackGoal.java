package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.goals;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.ZoltraakAttackEntity;
import com.github.sculkhorde.util.TickUnits;

public class ZoltraakAttackGoal extends ReaperCastSpellGoal
{


    public ZoltraakAttackGoal(SculkSoulReaperEntity mob) {
        super(mob);
    }

    @Override
    protected int getPreAttackDelay() {
        return TickUnits.convertSecondsToTicks(0.72F);
    }

    @Override
    protected void playPreAttackAnimation()
    {
        getReaper().triggerAnim(SculkSoulReaperEntity.COMBAT_ATTACK_ANIMATION_CONTROLLER_ID, SculkSoulReaperEntity.ZOLTRAAK_SPELL_USE_ID);
    }

    @Override
    protected void doAttackTick() {
        ZoltraakAttackEntity.castZoltraakOnEntity(mob, mob.getTarget(), mob.getEyePosition());
        setPostAttack(true);
    }
}