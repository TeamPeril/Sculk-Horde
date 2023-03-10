package com.github.sculkhorde.common.entity.projectile;

import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.core.ItemRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.particles.ParticleTypes;
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

    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            this.level.addParticle(ParticleTypes.NAUTILUS, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }
}
