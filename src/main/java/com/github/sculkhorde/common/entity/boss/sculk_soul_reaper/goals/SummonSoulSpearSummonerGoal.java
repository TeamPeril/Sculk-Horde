package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.goals;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulSpearSummonerAttackEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SummonSoulSpearSummonerGoal extends ReaperCastSpellGoal
{
    public SummonSoulSpearSummonerGoal(SculkSoulReaperEntity mob) {
        super(mob);
    }

    @Override
    protected boolean mustSeeTarget() {
        return false;
    }

    @Override
    public void start()
    {
        super.start();
        if(mob.level.isClientSide())
        {
            return;
        }

        this.mob.getNavigation().stop();
        EntityType.LIGHTNING_BOLT.spawn((ServerLevel) mob.level, (ItemStack)null, (Player)null, mob.blockPosition().above(50), MobSpawnType.SPAWNER, false, false);
    }

    @Override
    protected void doAttackTick() {
        summonSoulSpearSummoner();
        setSpellCompleted();
    }

    public void summonSoulSpearSummoner()
    {

        SoulSpearSummonerAttackEntity summonerEntity =  new SoulSpearSummonerAttackEntity(mob.level, mob);
        summonerEntity.setPos(mob.position().add(0, mob.getEyeHeight() + 5, 0));

        mob.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 1.0F / (mob.getRandom().nextFloat() * 0.4F + 0.8F));
        mob.level.addFreshEntity(summonerEntity);

    }
}