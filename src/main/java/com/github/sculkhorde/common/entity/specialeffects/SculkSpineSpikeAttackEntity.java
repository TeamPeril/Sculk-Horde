package com.github.sculkhorde.common.entity.specialeffects;

import com.github.sculkhorde.util.TickUnits;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SculkSpineSpikeAttackEntity extends Entity implements TraceableEntity {

    public static final int LIFE_IN_TICKS = TickUnits.convertSecondsToTicks(3);
    public static final int ATTACK_DELAY_TICKS = TickUnits.convertSecondsToTicks(1.5F);
    @Nullable
    private LivingEntity owner;
    @Nullable
    private UUID ownerUUID;

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
}
