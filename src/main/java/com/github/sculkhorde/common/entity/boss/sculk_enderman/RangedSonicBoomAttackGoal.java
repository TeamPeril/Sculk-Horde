package com.github.sculkhorde.common.entity.boss.sculk_enderman;

import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class RangedSonicBoomAttackGoal extends Goal
{
    private final Mob mob;
    protected int maxAttackDuration = 0;
    protected int elapsedAttackDuration = 0;
    protected final int executionCooldown = TickUnits.convertSecondsToTicks(5);
    protected int ticksElapsed = executionCooldown;


    public RangedSonicBoomAttackGoal(PathfinderMob mob, int durationInTicks) {
        this.mob = mob;
        maxAttackDuration = durationInTicks;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private SculkEndermanEntity getSculkEnderman()
    {
        return (SculkEndermanEntity)this.mob;
    }

    @Override
    public boolean canUse()
    {
        ticksElapsed++;

        if(getSculkEnderman().isSpecialAttackOnCooldown() || mob.getTarget() == null)
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
        return elapsedAttackDuration < maxAttackDuration;
    }

    @Override
    public void start()
    {
        super.start();
        this.mob.getNavigation().stop();
        // Teleport the enderman away from the mob
        getSculkEnderman().teleportAwayFromEntity(mob.getTarget());
        getSculkEnderman().stayInSpecificRangeOfTarget(16, 32);
        getSculkEnderman().triggerAnim("attack_controller", "fireball_shoot_animation");
        mob.playSound(SoundEvents.WARDEN_SONIC_CHARGE, 3.0F, 1.0F);
    }

    @Override
    public void tick()
    {
        super.tick();



        elapsedAttackDuration++;
        if(elapsedAttackDuration == 10)
        {
            performRangedAttack(mob.getTarget());
        }
    }

    @Override
    public void stop()
    {
        super.stop();
        getSculkEnderman().resetSpecialAttackCooldown();
        elapsedAttackDuration = 0;
        ticksElapsed = 0;
        getSculkEnderman().canTeleport = true;
    }
    public void performRangedAttack(LivingEntity targetEntity)
    {

        if(targetEntity == null)
        {
            return;
        }

        mob.getLookControl().setLookAt(targetEntity.position());
        Vec3 vec3 = mob.getEyePosition();
        Vec3 vec31 = targetEntity.getEyePosition().subtract(vec3);
        Vec3 vec32 = vec31.normalize();

        for(int i = 1; i < Mth.floor(vec31.length()) + 7; ++i) {
            Vec3 vec33 = vec3.add(vec32.scale((double)i));
            ((ServerLevel)mob.level()).sendParticles(ParticleTypes.SONIC_BOOM, vec33.x, vec33.y, vec33.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        mob.playSound(SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 1.0F);
        float damage = targetEntity.getMaxHealth() > 50.0F && targetEntity.getArmorValue() > 5F ? (targetEntity.getMaxHealth()/4F) + 10.0F : 10.0F;
        targetEntity.hurt(((ServerLevel)mob.level()).damageSources().explosion(null, mob), damage);
        double d1 = 0.5D * (1.0D - targetEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        double d0 = 2.5D * (1.0D - targetEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        targetEntity.push(vec32.x() * d0, vec32.y() * d1, vec32.z() * d0);
    }
}
