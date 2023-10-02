package com.github.sculkhorde.common.entity.boss.sculk_enderman;

import com.github.sculkhorde.common.entity.boss.SpecialEffectEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.keyframe.event.CustomInstructionKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class SculkSpineSpikeAttackEntity extends SpecialEffectEntity implements TraceableEntity, GeoEntity {

    public static int LIFE_IN_TICKS = TickUnits.convertSecondsToTicks(2);
    public static int ATTACK_DELAY_TICKS = TickUnits.convertSecondsToTicks(0.5F); // Had to eye ball this value
    @Nullable
    private LivingEntity owner;
    @Nullable
    private UUID ownerUUID;
    private int lifeTicks = 0;
    private boolean clientSideAttackStarted;

    public SculkSpineSpikeAttackEntity(EntityType<? extends SculkSpineSpikeAttackEntity> entityType, Level level) {
        super(entityType, level);
        triggerAnim("attack_controller", "attack_animation");
        ATTACK_ANIMATION_CONTROLLER.setAnimationSpeed(0.0F);
    }

    public SculkSpineSpikeAttackEntity(LivingEntity owner, double x, double y, double z) {
        super(ModEntities.SCULK_SPINE_SPIKE_ATTACK.get(), owner.level);
        this.setPos(x, y, z);
        this.owner = owner;
        this.ownerUUID = owner.getUUID();

    }


    public void setAppearanceDelay(int delay) {
        this.lifeTicks -= delay;
    }

    private void dealDamageTo(LivingEntity targetEntity) {
        LivingEntity livingentity = this.getOwner();
        if (targetEntity.isAlive() && !targetEntity.isInvulnerable() && targetEntity != livingentity)
        {

            if (livingentity == null)
            {
                targetEntity.hurt(this.damageSources().generic(), 6.0F);
                return;
            }

            if (livingentity.isAlliedTo(targetEntity))
            {
                return;
            }

            if(EntityAlgorithms.isSculkLivingEntity.test(targetEntity))
            {
                return;
            }

            targetEntity.hurt(this.damageSources().indirectMagic(this, livingentity), 6.0F);
            // Give weakness and levetation
            targetEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, TickUnits.convertMinutesToTicks(1), 0));
            targetEntity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, TickUnits.convertSecondsToTicks(20), 0));

        }
    }

    public void hurtTouchingEntities()
    {
        for(LivingEntity livingEntity : this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.5D)))
        {
            if(livingEntity != this.getOwner())
            {
                this.dealDamageTo(livingEntity);
            }
        }
    }


    public void tick()
    {
        super.tick();

        if(lifeTicks == 0)
        {
            ATTACK_ANIMATION_CONTROLLER.setAnimationSpeed(1.0F);
        }

        this.lifeTicks++;
        if (this.level.isClientSide)
        {
            if (this.clientSideAttackStarted)
            {
                if (this.lifeTicks == ATTACK_DELAY_TICKS) {

                }
            }
            return;
        }

        if (this.lifeTicks == ATTACK_DELAY_TICKS) {
            hurtTouchingEntities();
        }

        if(this.lifeTicks > LIFE_IN_TICKS || ATTACK_ANIMATION_CONTROLLER.hasAnimationFinished())
        {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    public void handleEntityEvent(byte b) {
        super.handleEntityEvent(b);
        this.clientSideAttackStarted = true;
        if (!this.isSilent()) {
            this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.EVOKER_FANGS_ATTACK, this.getSoundSource(), 1.0F, this.random.nextFloat() * 0.2F + 0.85F, false);
        }
    }

    private static final RawAnimation ATTACK_ANIMATION = RawAnimation.begin().thenPlay("misc.living");

    // ### GECKOLIB Animation Code ###
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final AnimationController ATTACK_ANIMATION_CONTROLLER = new AnimationController<>(this, "attack_controller", state -> PlayState.STOP)
            .triggerableAnim("attack_animation", ATTACK_ANIMATION)
            .setCustomInstructionKeyframeHandler(this::instructionListener);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(ATTACK_ANIMATION_CONTROLLER);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private <ENTITY extends GeoEntity> void instructionListener(CustomInstructionKeyframeEvent<ENTITY> event) {
        if(event.getKeyframeData().getInstructions().contains("DoDamageInstruction"))
        {
            if(this.level.isClientSide())
            {
                for(int i = 0; i < 12; ++i)
                {
                    double d0 = this.getX() + (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.getBbWidth() * 0.5D;
                    double d1 = this.getY() + 0.05D + this.random.nextDouble();
                    double d2 = this.getZ() + (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.getBbWidth() * 0.5D;
                    double d3 = (this.random.nextDouble() * 2.0D - 1.0D) * 0.3D;
                    double d4 = 0.3D + this.random.nextDouble() * 0.3D;
                    double d5 = (this.random.nextDouble() * 2.0D - 1.0D) * 0.3D;
                    this.level.addParticle(ParticleTypes.CRIT, d0, d1 + 1.0D, d2, d3, d4, d5);
                }
            }
        }
    }

}
