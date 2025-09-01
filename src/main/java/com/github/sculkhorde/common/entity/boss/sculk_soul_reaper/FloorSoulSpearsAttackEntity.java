package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper;

import com.github.sculkhorde.common.entity.boss.SpecialEffectEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModSounds;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.SoundUtil;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.keyframe.event.CustomInstructionKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FloorSoulSpearsAttackEntity extends SpecialEffectEntity implements TraceableEntity, GeoEntity {

    public static int LIFE_IN_TICKS = TickUnits.convertSecondsToTicks(2);
    public static int DAMAGE_DELAY_TICKS = TickUnits.convertSecondsToTicks(0.5F); // Had to eye ball this value
    private int lifeTicks = 0;



    public FloorSoulSpearsAttackEntity(EntityType<? extends FloorSoulSpearsAttackEntity> entityType, Level level) {
        super(entityType, level);
        triggerAnim("attack_controller", "underground");
    }

    public FloorSoulSpearsAttackEntity(LivingEntity owner, double x, double y, double z, int delay) {
        this(ModEntities.FLOOR_SOUL_SPEARS.get(), owner.level());
        this.setPos(x, y, z);
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
            // Give weakness and levitation
            EntityAlgorithms.applyEffectToTarget(targetEntity, MobEffects.WEAKNESS, TickUnits.convertMinutesToTicks(1), 0);
        }
    }

    public void hurtTouchingEntities()
    {
        for(LivingEntity livingEntity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.5D)))
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

        if(level().isClientSide())
        {
            return;
        }

        if(lifeTicks == 0)
        {
            triggerAnim("attack_controller", "emerge");
            SoundUtil.playHostileSoundInLevel(level(), blockPosition(), ModSounds.SOUL_SPEAR_EMERGE.get());
        }

        this.lifeTicks++;

        if (this.lifeTicks == DAMAGE_DELAY_TICKS) {
            hurtTouchingEntities();
        }

        if(this.lifeTicks > LIFE_IN_TICKS || ATTACK_ANIMATION_CONTROLLER.hasAnimationFinished())
        {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    public void handleEntityEvent(byte b) {
        super.handleEntityEvent(b);
    }

    private static final RawAnimation ATTACK_ANIMATION = RawAnimation.begin().thenPlay("emerge");
    private static final RawAnimation UNDERGROUND_ANIMATION = RawAnimation.begin().thenPlay("underground");

    // ### GECKOLIB Animation Code ###
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final AnimationController ATTACK_ANIMATION_CONTROLLER = new AnimationController<>(this, "attack_controller", state -> PlayState.STOP)
            .triggerableAnim("emerge", ATTACK_ANIMATION)
            .triggerableAnim("underground", UNDERGROUND_ANIMATION)
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
        if(event.getKeyframeData().getInstructions().contains("DoVFXInstruction"))
        {
            if(this.level().isClientSide())
            {
                for(int i = 0; i < 12; ++i)
                {
                    Vec3 pos = position();

                    double d0 = this.getX() + (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.getBbWidth() * 0.5D;
                    double d1 = this.getY() + 0.05D + this.random.nextDouble();
                    double d2 = this.getZ() + (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.getBbWidth() * 0.5D;
                    double d3 = (this.random.nextDouble() * 2.0D - 1.0D) * 0.3D;
                    double d4 = 0.3D + this.random.nextDouble() * 0.3D;
                    double d5 = (this.random.nextDouble() * 2.0D - 1.0D) * 0.3D;
                    this.level().addParticle(ParticleTypes.CRIT, d0, d1 + 1.0D, d2, d3, d4, d5);
                    SoundUtil.playHostileSoundInLevel(level(), blockPosition(), ModSounds.SOUL_SPEAR_EMERGE.get());
                }
            }
        }
    }

}
