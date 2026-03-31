package com.github.sculkhorde.common.entity.boss.angel_of_reaping.goals;

import com.github.sculkhorde.common.entity.boss.angel_of_reaping.AngelOfReapingEntity;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.SoulBlastAttackEntity;
import com.github.sculkhorde.common.entity.entity_debugging.IDebuggableGoal;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.sounds.SoundEvents;

import java.util.Optional;

public class SoulBlastAttackGoal extends ReaperCastSpellGoal implements IDebuggableGoal
{

    protected String reasonForNoStart = "None";

    public SoulBlastAttackGoal(AngelOfReapingEntity mob) {
        super(mob);
    }

    @Override
    public void start()
    {
        super.start();
        if(mob.level().isClientSide())
        {
            return;
        }

        this.mob.getNavigation().stop();
        //EntityType.LIGHTNING_BOLT.spawn((ServerLevel) mob.level(), BlockPos.containing(mob.getEyePosition()), MobSpawnType.SPAWNER);
    }

    @Override
    protected void doAttackTick() {
        summonAttackEntity();
        setPostAttack(true);
    }

    public void summonAttackEntity()
    {

        SoulBlastAttackEntity attackEntity =  new SoulBlastAttackEntity(mob.level(), mob);
        attackEntity.setPos(mob.position().add(0, mob.getEyeHeight() + 1, 0));

        mob.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 1.0F / (mob.getRandom().nextFloat() * 0.4F + 0.8F));
        mob.level().addFreshEntity(attackEntity);

    }


    @Override
    protected int getPreAttackDelay() {
        return TickUnits.convertSecondsToTicks(1F);
    }

    @Override
    protected void playAttackAnimation() {
        getReaper().triggerAnim(AngelOfReapingEntity.COMBAT_ATTACK_ANIMATION_CONTROLLER_ID, AngelOfReapingEntity.ATTACK_SPELL_USE_ID);
    }

    @Override
    public Optional<String> getLastReasonForGoalNoStart() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getGoalName() {
        return Optional.of("SoulBlastGoal");
    }

    @Override
    public long getLastTimeOfGoalExecution() {
        return -1;
    }

    @Override
    public long getTimeRemainingBeforeCooldownOver() {
        return -1;
    }
}