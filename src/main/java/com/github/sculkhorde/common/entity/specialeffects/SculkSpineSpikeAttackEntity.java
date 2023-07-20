package com.github.sculkhorde.common.entity.specialeffects;

import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class SculkSpineSpikeAttackEntity extends Entity implements TraceableEntity, GeoEntity {

    public static final int LIFE_IN_TICKS = TickUnits.convertSecondsToTicks(3);
    public static final int ATTACK_DELAY_TICKS = TickUnits.convertSecondsToTicks(1.5F);
    @Nullable
    private LivingEntity owner;
    @Nullable
    private UUID ownerUUID;
    private int lifeTicks = LIFE_IN_TICKS;
    private boolean clientSideAttackStarted;

    public SculkSpineSpikeAttackEntity(EntityType<? extends SculkSpineSpikeAttackEntity> entityType, Level level) {
        super(entityType, level);
    }

    public SculkSpineSpikeAttackEntity(Level level, LivingEntity owner, double x, double y, double z) {
        super(EntityRegistry.SCULK_SPINE_SPIKE_ATTACK.get(), owner.level());
        this.setPos(x, y, z);
        this.owner = owner;
        this.ownerUUID = owner.getUUID();
    }

    // Getters

    public void setOwner(@Nullable LivingEntity p_36939_) {
        this.owner = p_36939_;
        this.ownerUUID = p_36939_ == null ? null : p_36939_.getUUID();
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.owner == null && this.ownerUUID != null && this.level() instanceof ServerLevel) {
            Entity entity = ((ServerLevel)this.level()).getEntity(this.ownerUUID);
            if (entity instanceof LivingEntity) {
                this.owner = (LivingEntity)entity;
            }
        }

        return this.owner;
    }

    private void dealDamageTo(LivingEntity p_36945_) {
        LivingEntity livingentity = this.getOwner();
        if (p_36945_.isAlive() && !p_36945_.isInvulnerable() && p_36945_ != livingentity) {
            if (livingentity == null) {
                p_36945_.hurt(this.damageSources().magic(), 6.0F);
            } else {
                if (livingentity.isAlliedTo(p_36945_)) {
                    return;
                }

                p_36945_.hurt(this.damageSources().indirectMagic(this, livingentity), 6.0F);
            }

        }
    }

    public void tick()
    {
        super.tick();
        if (this.level().isClientSide) {
            if (this.clientSideAttackStarted) {
                --this.lifeTicks;
                if (this.lifeTicks == ATTACK_DELAY_TICKS) {
                    for(int i = 0; i < 12; ++i) {
                        double d0 = this.getX() + (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.getBbWidth() * 0.5D;
                        double d1 = this.getY() + 0.05D + this.random.nextDouble();
                        double d2 = this.getZ() + (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.getBbWidth() * 0.5D;
                        double d3 = (this.random.nextDouble() * 2.0D - 1.0D) * 0.3D;
                        double d4 = 0.3D + this.random.nextDouble() * 0.3D;
                        double d5 = (this.random.nextDouble() * 2.0D - 1.0D) * 0.3D;
                        this.level().addParticle(ParticleTypes.CRIT, d0, d1 + 1.0D, d2, d3, d4, d5);
                    }
                }
            }
            return;
        }

        if (this.lifeTicks == ATTACK_DELAY_TICKS) {
            for(LivingEntity livingentity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.2D, 0.0D, 0.2D))) {
                this.dealDamageTo(livingentity);
            }
        }

        if(--this.lifeTicks < 0)
        {
            this.remove(RemovalReason.DISCARDED);
            return;
        }
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
    }

    public void handleEntityEvent(byte b) {
        super.handleEntityEvent(b);
        this.clientSideAttackStarted = true;
        if (!this.isSilent()) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.EVOKER_FANGS_ATTACK, this.getSoundSource(), 1.0F, this.random.nextFloat() * 0.2F + 0.85F, false);
        }
    }

    // ### GECKOLIB Animation Code ###
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericIdleController(this));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

}
