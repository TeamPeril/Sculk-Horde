package com.github.sculkhorde.common.entity.specialeffects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class SpecialEffectEntity extends Entity
{
    public LivingEntity sourceEntity;
    protected boolean hasSyncedSourceEntity = false;
    private static final EntityDataAccessor<Optional<UUID>> SOURCE_ENTITY = SynchedEntityData.defineId(SpecialEffectEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public SpecialEffectEntity(EntityType<?> entityType, Level level)
    {
        super(entityType, level);
    }

    public SpecialEffectEntity(EntityType<?> entityType, Level level, LivingEntity sourceEntity)
    {
        super(entityType, level);
        this.sourceEntity = sourceEntity;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    public Optional<UUID> getSourceEntityID() {
        return getEntityData().get(SOURCE_ENTITY);
    }

    public void setSourceEntityID(UUID id) {
        getEntityData().set(SOURCE_ENTITY, Optional.of(id));
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public void push(Entity entityIn) {
    }

    @Override
    public void tick()
    {
        super.tick();

        /*
        if (!level.isClientSide() && getSourceEntityID().isPresent() && sourceEntity == null) {
            Entity casterEntity = ((ServerLevel)this.level).getEntity(getSourceEntityID().get());
            if (casterEntity instanceof LivingEntity) {
                sourceEntity = (LivingEntity) casterEntity;
            }
            hasSyncedSourceEntity = true;
        }
        */
    }

    public void link(Entity entity) {
        if (entity instanceof LivingEntity) {
            sourceEntity = (LivingEntity) entity;
        }
        hasSyncedSourceEntity = true;
    }


    public Packet<ClientGamePacketListener> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        //setSourceEntityID(compound.getUUID("source_entity"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        /*
        if (getSourceEntityID().isPresent()) {
            //compound.putUUID("source_entity", getSourceEntityID().get());
        }

         */
    }

    public List<LivingEntity> getEntityLivingBaseNearby(double radius) {
        return getEntitiesNearby(LivingEntity.class, radius);
    }

    public <T extends Entity> List<T> getEntitiesNearby(Class<T> entityClass, double r) {
        return level.getEntitiesOfClass(entityClass, getBoundingBox().inflate(r, r, r), e -> e != this && distanceTo(e) <= r + e.getBbWidth() / 2f);
    }

    public <T extends Entity> List<T> getEntitiesNearbyCube(Class<T> entityClass, double r) {
        return level.getEntitiesOfClass(entityClass, getBoundingBox().inflate(r, r, r), e -> e != this);
    }

    public boolean raytraceCheckEntity(Entity entity) {
        Vec3 from = this.position();
        int numChecks = 3;
        for (int i = 0; i < numChecks; i++) {
            float increment = entity.getBbHeight() / (numChecks + 1);
            Vec3 to = entity.position().add(0, increment * (i + 1), 0);
            BlockHitResult result = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (result.getType() != HitResult.Type.BLOCK)
            {
                return true;
            }
        }
        return false;
    }


}
