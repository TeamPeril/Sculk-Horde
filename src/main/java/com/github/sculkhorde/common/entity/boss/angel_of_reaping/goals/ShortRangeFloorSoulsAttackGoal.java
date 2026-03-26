package com.github.sculkhorde.common.entity.boss.angel_of_reaping.goals;

import com.github.sculkhorde.common.entity.boss.angel_of_reaping.AngelOfReapingEntity;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

public class ShortRangeFloorSoulsAttackGoal extends ReaperCastSpellGoal
{
    public ShortRangeFloorSoulsAttackGoal(AngelOfReapingEntity mob) {
        super(mob);
    }

    @Override
    protected void doAttackTick() {
        spawnSoulSuckersOnFloorInCircle(0, 1);
        spawnSoulSuckersOnFloorInCircle(6, 6);
        setPostAttack(true);
    }

    public void spawnSoulSuckersOnFloorInCircle(int radius, int amount)
    {
        ArrayList<Vec3> pos = BlockAlgorithms.getPointsOnCircumferenceVec3(mob.position(), radius, amount);

        for(Vec3 position: pos)
        {
            AreaEffectCloud cloud = new AreaEffectCloud(mob.level(), position.x, position.y, position.z);
            cloud.setOwner(mob);
            cloud.setRadius(3);
            cloud.setDuration(TickUnits.convertSecondsToTicks(20));
            cloud.addEffect(new MobEffectInstance(MobEffects.HARM));
            cloud.setParticle(ParticleTypes.SCULK_SOUL);
            mob.level().addFreshEntity(cloud);
        }
    }

}
