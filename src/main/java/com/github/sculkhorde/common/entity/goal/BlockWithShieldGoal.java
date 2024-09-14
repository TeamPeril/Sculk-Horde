package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.LivingArmorEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;

public class BlockWithShieldGoal extends Goal {
    public final LivingArmorEntity entity;

    public BlockWithShieldGoal(LivingArmorEntity guard) {
        this.entity = guard;
    }

    @Override
    public boolean canUse() {
        return entity.hasShield() && canRaiseShield() && entity.shieldCoolDown == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        if (entity.hasShield()) {
            entity.startUsingItem(InteractionHand.OFF_HAND);
        }
    }


    protected boolean canRaiseShield() {
        LivingEntity target = entity.getTarget();
        if (target != null && entity.shieldCoolDown == 0) {
            return entity.distanceTo(target) <= 4.0D || target instanceof RangedAttackMob && target.distanceTo(entity) >= 5.0D;
        }
        return false;
    }
}
