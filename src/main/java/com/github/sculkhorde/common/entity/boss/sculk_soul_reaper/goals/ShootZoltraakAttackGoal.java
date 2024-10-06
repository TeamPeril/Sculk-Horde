package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.goals;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity;
import com.github.sculkhorde.core.ModSounds;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ShootZoltraakAttackGoal extends Goal
{
    private final Mob mob;
    protected final int executionCooldown = TickUnits.convertSecondsToTicks(5);
    protected int ticksElapsed = executionCooldown;

    protected final int baseCastingTime = TickUnits.convertSecondsToTicks(1);
    protected int castingTime = 0;
    protected boolean spellCasted = false;

    protected float DAMAGE = 8F;



    public ShootZoltraakAttackGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private SculkSoulReaperEntity getEntity()
    {
        return (SculkSoulReaperEntity)this.mob;
    }

    protected int getCastingTimeElapsed()
    {
        return castingTime;
    }


    @Override
    public boolean canUse()
    {
        ticksElapsed++;

        if(mob.getTarget() == null)
        {
            return false;
        }

        if(ticksElapsed < executionCooldown)
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean canContinueToUse()
    {
        return !spellCasted && mob.getTarget() != null;
    }

    @Override
    public void start()
    {
        super.start();

        if(mob.level().isClientSide())
        {
            return;
        }

        mob.level().playSound(mob,mob.blockPosition(), SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 1.0F, 1.0F);

        //getEntity().triggerAnim("attack_controller", "fireball_sky_summon_animation");
        //getEntity().triggerAnim("twitch_controller", "fireball_sky_twitch_animation");
    }

    @Override
    public void tick()
    {
        super.tick();

        if(mob.level().isClientSide())
        {
            return;
        }

        if(getCastingTimeElapsed() < baseCastingTime)
        {
            castingTime++;
            return;
        }

        if(spellCasted)
        {
            return;
        }

        shootProjectileAtTarget(DAMAGE, 0.5F, 10F);
        spellCasted = true;
    }

    @Override
    public void stop()
    {
        super.stop();
        ticksElapsed = 0;
        spellCasted = false;
        castingTime = 0;
    }

    public double getRandomDoubleInRange(double min, double max)
    {
        return min + (mob.getRandom().nextFloat() * (max + min));
    }
    public int getRandomIntInRange(int min, int max)
    {
        return min + (mob.getRandom().nextInt() * (max + min));
    }

    public void shootProjectileAtTarget(float damage, float radius, float thickness)
    {

        if(mob.getTarget() == null)
        {
            return;
        }

        mob.getLookControl().setLookAt(mob.getTarget().position());
        Vec3 vec3 = mob.getEyePosition();
        Vec3 vec31 = mob.getTarget().getEyePosition().subtract(vec3);
        Vec3 vec32 = vec31.normalize();

        for (int i = 1; i < Mth.floor(vec31.length()) + 7; ++i) {
            Vec3 vec33 = vec3.add(vec32.scale((double) i));

            // Create a circle of particles around vec33
            for (int j = 0; j < thickness; ++j) {
                double angle = 2 * Math.PI * j / thickness;
                double xOffset = radius * Math.cos(angle);
                double zOffset = radius * Math.sin(angle);
                ((ServerLevel) mob.level()).sendParticles(ParticleTypes.SOUL_FIRE_FLAME, vec33.x + xOffset, vec33.y, vec33.z + zOffset, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }


        mob.level().playSound(mob,mob.blockPosition(), ModSounds.ZOLTRAAK_ATTACK.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
        mob.getTarget().hurt(mob.damageSources().magic(), damage);

    }

}
//projectileEntity.shoot(d0, d1 + d3 * (double)0.2F, d2, 1.6F, (float)(14 - mob.level().getDifficulty().getId() * 4));