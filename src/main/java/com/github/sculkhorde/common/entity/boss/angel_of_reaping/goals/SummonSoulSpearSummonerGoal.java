package com.github.sculkhorde.common.entity.boss.angel_of_reaping.goals;

import com.github.sculkhorde.common.entity.boss.angel_of_reaping.AngelOfReapingEntity;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.SoulSpearSummonerAttackEntity;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.sounds.SoundEvents;

public class SummonSoulSpearSummonerGoal extends ReaperCastSpellGoal
{
    public SummonSoulSpearSummonerGoal(AngelOfReapingEntity mob) {
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
        //EntityType.LIGHTNING_BOLT.spawn((ServerLevel) mob.level(), mob.blockPosition().above(50), MobSpawnType.SPAWNER);
    }

    @Override
    protected int getPreAttackDelay() {
        return TickUnits.convertSecondsToTicks(0.56F);
    }

    @Override
    protected void doAttackTick() {
        summonSoulSpearSummoner();
        setPostAttack(true);
    }

    public void summonSoulSpearSummoner()
    {

        SoulSpearSummonerAttackEntity summonerEntity =  new SoulSpearSummonerAttackEntity(mob.level(), mob);
        summonerEntity.setPos(mob.position().add(0, mob.getEyeHeight() + 5, 0));

        mob.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 1.0F / (mob.getRandom().nextFloat() * 0.4F + 0.8F));
        mob.level().addFreshEntity(summonerEntity);

    }

    @Override
    protected void playPreAttackAnimation()
    {
        getReaper().triggerAnim(AngelOfReapingEntity.COMBAT_ATTACK_ANIMATION_CONTROLLER_ID, AngelOfReapingEntity.ATTACK_SPELL_USE_ID);
    }
}