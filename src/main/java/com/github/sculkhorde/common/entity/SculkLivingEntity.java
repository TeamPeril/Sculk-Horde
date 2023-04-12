package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.Level;

public class SculkLivingEntity extends Monster {

    public static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(SculkLivingEntity.class,
            EntityDataSerializers.INT);


    /**
     * The Constructor <br>
     * This class is only used for classification.
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkLivingEntity(EntityType<? extends SculkLivingEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    /**
     * If a sculk living entity despawns, refund it's current health to the sculk hoard
     */
    @Override
    public void onRemovedFromWorld() {
        SculkHorde.gravemind.getGravemindMemory().addSculkAccumulatedMass((int) this.getHealth());
        super.onRemovedFromWorld();
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }

    public int getAttckingState() {
        return this.entityData.get(STATE);
    }

    public void setAttackingState(int time) {
        this.entityData.set(STATE, time);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, 0);
    }
}
