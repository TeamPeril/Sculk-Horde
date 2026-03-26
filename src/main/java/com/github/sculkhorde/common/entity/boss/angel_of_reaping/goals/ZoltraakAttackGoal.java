package com.github.sculkhorde.common.entity.boss.angel_of_reaping.goals;

import com.github.sculkhorde.common.entity.boss.angel_of_reaping.AngelOfReapingEntity;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.ZoltraakAttackEntity;
import com.github.sculkhorde.util.TickUnits;

public class ZoltraakAttackGoal extends ReaperCastSpellGoal
{


    public ZoltraakAttackGoal(AngelOfReapingEntity mob) {
        super(mob);
    }

    @Override
    protected int getPreAttackDelay() {
        return TickUnits.convertSecondsToTicks(0.72F);
    }

    @Override
    protected void playPreAttackAnimation()
    {
        getReaper().triggerAnim(AngelOfReapingEntity.COMBAT_ATTACK_ANIMATION_CONTROLLER_ID, AngelOfReapingEntity.ZOLTRAAK_SPELL_USE_ID);
    }

    @Override
    protected void doAttackTick() {
        ZoltraakAttackEntity.castZoltraakOnEntity(mob, mob.getTarget(), mob.getEyePosition());
        setPostAttack(true);
    }
}