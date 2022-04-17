package com.github.sculkhoard.common.entity.projectile;

import com.github.sculkhoard.core.EntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.FMLPlayMessages;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public abstract class ProjectileEntity extends Entity {

    private UUID ownerUUID;
    private int ownerNetworkId;
    private boolean leftOwner;

    ProjectileEntity(EntityType<? extends net.minecraft.entity.projectile.ProjectileEntity> p_i231584_1_, World p_i231584_2_) {
        super(p_i231584_1_, p_i231584_2_);
    }

    public void setOwner(@Nullable Entity pEntity) {
        if (pEntity != null) {
            this.ownerUUID = pEntity.getUUID();
            this.ownerNetworkId = pEntity.getId();
        }

    }

    @Nullable
    public Entity getOwner() {
        if (this.ownerUUID != null && this.level instanceof ServerWorld) {
            return ((ServerWorld)this.level).getEntity(this.ownerUUID);
        } else {
            return this.ownerNetworkId != 0 ? this.level.getEntity(this.ownerNetworkId) : null;
        }
    }

    protected void addAdditionalSaveData(CompoundNBT pCompound) {
        if (this.ownerUUID != null) {
            pCompound.putUUID("Owner", this.ownerUUID);
        }

        if (this.leftOwner) {
            pCompound.putBoolean("LeftOwner", true);
        }

    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void readAdditionalSaveData(CompoundNBT pCompound) {
        if (pCompound.hasUUID("Owner")) {
            this.ownerUUID = pCompound.getUUID("Owner");
        }

        this.leftOwner = pCompound.getBoolean("LeftOwner");
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void tick() {
        if (!this.leftOwner) {
            this.leftOwner = this.checkLeftOwner();
        }

        super.tick();
    }

    private boolean checkLeftOwner() {
        Entity entity = this.getOwner();
        if (entity != null) {
            for(Entity entity1 : this.level.getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), (p_234613_0_) -> {
                return !p_234613_0_.isSpectator() && p_234613_0_.isPickable();
            })) {
                if (entity1.getRootVehicle() == entity.getRootVehicle()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Similar to setArrowHeading, it's point the throwable entity to a x, y, z direction.
     */
    public void shoot(double pX, double pY, double pZ, float pVelocity, float pInaccuracy) {
        Vector3d vector3d = (new Vector3d(pX, pY, pZ)).normalize().add(this.random.nextGaussian() * (double)0.0075F * (double)pInaccuracy, this.random.nextGaussian() * (double)0.0075F * (double)pInaccuracy, this.random.nextGaussian() * (double)0.0075F * (double)pInaccuracy).scale((double)pVelocity);
        this.setDeltaMovement(vector3d);
        float f = MathHelper.sqrt(getHorizontalDistanceSqr(vector3d));
        this.yRot = (float)(MathHelper.atan2(vector3d.x, vector3d.z) * (double)(180F / (float)Math.PI));
        this.xRot = (float)(MathHelper.atan2(vector3d.y, (double)f) * (double)(180F / (float)Math.PI));
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;
    }

    public void shootFromRotation(Entity pShooter, float pX, float pY, float pZ, float pVelocity, float pInaccuracy) {
        float f = -MathHelper.sin(pY * ((float)Math.PI / 180F)) * MathHelper.cos(pX * ((float)Math.PI / 180F));
        float f1 = -MathHelper.sin((pX + pZ) * ((float)Math.PI / 180F));
        float f2 = MathHelper.cos(pY * ((float)Math.PI / 180F)) * MathHelper.cos(pX * ((float)Math.PI / 180F));
        this.shoot((double)f, (double)f1, (double)f2, pVelocity, pInaccuracy);
        Vector3d vector3d = pShooter.getDeltaMovement();
        this.setDeltaMovement(this.getDeltaMovement().add(vector3d.x, pShooter.isOnGround() ? 0.0D : vector3d.y, vector3d.z));
    }

    /**
     * Called when this EntityFireball hits a block or entity.
     */
    protected void onHit(RayTraceResult pResult) {
        RayTraceResult.Type raytraceresult$type = pResult.getType();
        if (raytraceresult$type == RayTraceResult.Type.ENTITY) {
            this.onHitEntity((EntityRayTraceResult)pResult);
        } else if (raytraceresult$type == RayTraceResult.Type.BLOCK) {
            this.onHitBlock((BlockRayTraceResult)pResult);
        }

    }

    /**
     * Called when the arrow hits an entity
     */
    protected void onHitEntity(EntityRayTraceResult pResult) {
    }

    protected void onHitBlock(BlockRayTraceResult pResult) {
        BlockState blockstate = this.level.getBlockState(pResult.getBlockPos());
        //TODO fix this
        //blockstate.onProjectileHit(this.level, blockstate, pResult, this);
    }

    /**
     * Updates the entity motion clientside, called by packets from the server
     */
    @OnlyIn(Dist.CLIENT)
    public void lerpMotion(double pX, double pY, double pZ) {
        this.setDeltaMovement(pX, pY, pZ);
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            float f = MathHelper.sqrt(pX * pX + pZ * pZ);
            this.xRot = (float)(MathHelper.atan2(pY, (double)f) * (double)(180F / (float)Math.PI));
            this.yRot = (float)(MathHelper.atan2(pX, pZ) * (double)(180F / (float)Math.PI));
            this.xRotO = this.xRot;
            this.yRotO = this.yRot;
            this.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, this.xRot);
        }

    }

    protected boolean canHitEntity(Entity pTarget) {
        if (!pTarget.isSpectator() && pTarget.isAlive() && pTarget.isPickable()) {
            Entity entity = this.getOwner();
            return entity == null || this.leftOwner || !entity.isPassengerOfSameVehicle(pTarget);
        } else {
            return false;
        }
    }

    protected void updateRotation() {
        Vector3d vector3d = this.getDeltaMovement();
        float f = MathHelper.sqrt(getHorizontalDistanceSqr(vector3d));
        this.xRot = lerpRotation(this.xRotO, (float)(MathHelper.atan2(vector3d.y, (double)f) * (double)(180F / (float)Math.PI)));
        this.yRot = lerpRotation(this.yRotO, (float)(MathHelper.atan2(vector3d.x, vector3d.z) * (double)(180F / (float)Math.PI)));
    }

    protected static float lerpRotation(float pCurrentRotation, float pTargetRotation) {
        while(pTargetRotation - pCurrentRotation < -180.0F) {
            pCurrentRotation -= 360.0F;
        }

        while(pTargetRotation - pCurrentRotation >= 180.0F) {
            pCurrentRotation += 360.0F;
        }

        return MathHelper.lerp(0.2F, pCurrentRotation, pTargetRotation);
    }
}
