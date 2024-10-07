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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ZoltraakAttackGoal extends Goal
{
    private final Mob mob;
    protected final int executionCooldown = TickUnits.convertSecondsToTicks(1);
    protected int ticksElapsed = executionCooldown;

    protected final int baseCastingTime = TickUnits.convertSecondsToTicks(1);
    protected int castingTime = 0;
    protected boolean spellCasted = false;

    protected float DAMAGE = 8F;



    public ZoltraakAttackGoal(PathfinderMob mob) {
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

        if(!mob.getSensing().hasLineOfSight(mob.getTarget()))
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

        shootZoltraakBeam(DAMAGE, 0.3F, 10F);
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

    public void shootZoltraakBeam(float damage, float radius, float thickness)
    {

        if(mob.getTarget() == null)
        {
            return;
        }

        mob.getLookControl().setLookAt(mob.getTarget().position());
        Vec3 startVector = mob.getEyePosition();
        Vec3 targetVector = mob.getTarget().getEyePosition().subtract(startVector);
        Vec3 direction = targetVector.normalize();

        // Perform ray trace
        HitResult hitResult = mob.level().clip(new ClipContext(startVector, startVector.add(targetVector), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob));

        Vec3 hitVector = hitResult.getLocation();

        Vec3 beamPath = hitVector.subtract(startVector);


        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = direction.cross(up).normalize();
        Vec3 forward = direction.cross(right).normalize();

        // Spawn Particles
        for (float i = 1; i < Mth.floor(beamPath.length()) + 1; i += 0.3F) {
            Vec3 vec33 = startVector.add(direction.scale((double) i));

            // Create a circle of particles around vec33
            for (int j = 0; j < thickness; ++j) {
                double angle = 2 * Math.PI * j / thickness;
                double xOffset = radius * Math.cos(angle);
                double zOffset = radius * Math.sin(angle);
                Vec3 offset = right.scale(xOffset).add(forward.scale(zOffset));
                ((ServerLevel) mob.level()).sendParticles(ParticleTypes.SOUL_FIRE_FLAME, vec33.x + offset.x, vec33.y + offset.y, vec33.z + offset.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }

        mob.level().playSound(mob,mob.blockPosition(), ModSounds.ZOLTRAAK_ATTACK.get(), SoundSource.HOSTILE, 1.0F, 1.0F);

        if(mob.getSensing().hasLineOfSight(mob.getTarget()))
        {
            mob.getTarget().hurt(mob.damageSources().magic(), damage);
        }
    }

}