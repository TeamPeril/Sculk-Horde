package com.github.sculkhorde.common.entity.boss.angel_of_reaping.goals;

import com.github.sculkhorde.common.entity.boss.angel_of_reaping.AngelOfReapingEntity;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.LivingArmorEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class MirrorPlayerGoal extends ReaperCastSpellGoal
{
    public MirrorPlayerGoal(AngelOfReapingEntity mob) {
        super(mob);
    }

    @Override
    public boolean canUse()
    {
        if(!super.canUse())
        {
            return false;
        }
        if(!(mob.getTarget() instanceof Player))
        {
            return false;
        }
        return true;
    }


    @Override
    protected void doAttackTick() {
        if(mob.level().isClientSide())
        {
            return;
        }

        shootFakeBeam(mob.getEyePosition(), mob, mob.getTarget(), 0.1F, 5F);
        LivingArmorEntity entity = new LivingArmorEntity(mob.level(), mob.position());
        mob.level().addFreshEntity(entity);
        setPostAttack(true);
    }
    public void shootFakeBeam(Vec3 origin, Mob shooter, LivingEntity target, float radius, float thickness)
    {
        if(target == null)
        {
            return;
        }

        shooter.getLookControl().setLookAt(target.position());
        Vec3 targetVector = shooter.getTarget().getEyePosition().subtract(origin);
        Vec3 direction = targetVector.normalize();
        Vec3 beamPath = targetVector.subtract(origin);


        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = direction.cross(up).normalize();
        Vec3 forward = direction.cross(right).normalize();

        // Spawn Particles
        for (float i = 1; i < Mth.floor(beamPath.length()) + 1; i += 0.3F) {
            Vec3 vec33 = origin.add(direction.scale((double) i));

            // Create a circle of particles around vec33
            for (int j = 0; j < thickness; ++j) {
                double angle = 2 * Math.PI * j / thickness;
                double xOffset = radius * Math.cos(angle);
                double zOffset = radius * Math.sin(angle);
                Vec3 offset = right.scale(xOffset).add(forward.scale(zOffset));
                ((ServerLevel) shooter.level()).sendParticles(ParticleTypes.TOTEM_OF_UNDYING, vec33.x + offset.x, vec33.y + offset.y, vec33.z + offset.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        shooter.level().playSound(shooter,shooter.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.HOSTILE, 1.0F, 1.0F);
    }
}