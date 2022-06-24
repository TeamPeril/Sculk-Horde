package com.github.sculkhoard.common.entity.projectile;

import com.github.sculkhoard.core.EntityRegistry;
import com.github.sculkhoard.core.ItemRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;


public class SculkAcidicProjectileEntity extends CustomItemProjectileEntity {

    private float damage = 50f;

    /** CONSTRUCTORS **/

    /**
     * Default Constructor
     * @param entityIn The Entity we are Shooting
     * @param worldIn The world the projectile will exist in
     */
    public SculkAcidicProjectileEntity(EntityType<? extends CustomItemProjectileEntity> entityIn, World worldIn) {
        super(entityIn, worldIn);
    }

    /**
     * Constructor
     * @param worldIn The World to spawn the projectile in
     */
    public SculkAcidicProjectileEntity(World worldIn,  LivingEntity shooterIn, float damageIn) {
        this(EntityRegistry.SCULK_ACIDIC_PROJECTILE_ENTITY, worldIn);
        this.setPos(shooterIn.getX(), shooterIn.getEyeY(), shooterIn.getZ());
        this.setOwner(shooterIn);
        this.setDamage(damageIn);
    }


    /** MODIFIERS **/

    /** ACCESSORS **/

    protected Item getDefaultItem() {
        return ItemRegistry.SCULK_ACIDIC_PROJECTILE.get();
    }


    /** EVENTS **/

}
