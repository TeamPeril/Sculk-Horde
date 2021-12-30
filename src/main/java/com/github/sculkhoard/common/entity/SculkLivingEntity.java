package com.github.sculkhoard.common.entity;

import com.github.sculkhoard.core.EntityRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.world.World;

public class SculkLivingEntity extends MonsterEntity {

    /**
     * The Constructor <br>
     * This class is only used for classification.
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkLivingEntity(EntityType<? extends SculkLivingEntity> type, World worldIn) {
        super(type, worldIn);
    }

}
