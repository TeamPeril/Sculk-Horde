package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public class SculkLivingEntity extends MonsterEntity {

    public static final DataParameter<Integer> STATE = EntityDataManager.defineId(SculkLivingEntity.class,
            DataSerializers.INT);


    /**
     * The Constructor <br>
     * This class is only used for classification.
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkLivingEntity(EntityType<? extends SculkLivingEntity> type, World worldIn) {
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
