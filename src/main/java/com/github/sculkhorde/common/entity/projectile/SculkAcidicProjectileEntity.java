package com.github.sculkhorde.common.entity.projectile;

import com.github.sculkhorde.common.effect.CorrodingEffect;
import com.github.sculkhorde.common.entity.SculkMiteEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModItems;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;


public class SculkAcidicProjectileEntity extends CustomItemProjectileEntity {

    /** CONSTRUCTORS **/

    /**
     * Default Constructor
     * @param entityIn The Entity we are Shooting
     * @param worldIn The world the projectile will exist in
     */
    public SculkAcidicProjectileEntity(EntityType<? extends CustomItemProjectileEntity> entityIn, Level worldIn) {
        super(entityIn, worldIn);
    }

    /**
     * Constructor
     * @param worldIn The World to spawn the projectile in
     */
    public SculkAcidicProjectileEntity(Level worldIn,  LivingEntity shooterIn, float damageIn) {
        this(ModEntities.SCULK_ACIDIC_PROJECTILE_ENTITY.get(), worldIn);
        this.setPos(shooterIn.getX(), shooterIn.getEyeY(), shooterIn.getZ());
        this.setOwner(shooterIn);
        this.setDamage(damageIn);
    }


    /** MODIFIERS **/

    /** ACCESSORS **/
    @Override
    protected Item getDefaultItem() {
        return ModItems.SCULK_ACIDIC_PROJECTILE.get();
    }

    /**
     * Used to determine what partciles this entity should create when it hits a wall.
     * @return The Particle Data
     */
    @Override
    protected ParticleOptions getParticle()
    {
        return new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModItems.SCULK_ACIDIC_PROJECTILE.get()));
    }

    @Override
    protected void onHitEntity(EntityHitResult raytrace) {
        super.onHitEntity(raytrace);

        if(this.level().isClientSide())
        {
            this.level().addParticle(getParticle(), this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            return;
        }

        if(this.getOwner() != null && raytrace.getEntity().getUUID().equals(this.getOwner().getUUID()))
        {
            return;
        }

        if(raytrace.getEntity() instanceof LivingEntity target)
        {
            LivingEntity owner = (LivingEntity) this.getOwner();

            if(owner != null)
            {
                if(EntityAlgorithms.isSculkLivingEntity.test(owner) && EntityAlgorithms.isSculkLivingEntity.test(target))
                {
                    return;
                }
            }

            if(isBeingShieldBlocked(target))
            {
                return;
            }

            CorrodingEffect.applyToEntity(owner, target, TickUnits.convertSecondsToTicks(5));
        }
    }

    public boolean isBeingShieldBlocked(LivingEntity target)
    {
        if (target.isBlocking()) {
            Vec3 vec32 = this.position();
            if (vec32 != null) {
                Vec3 targetViewVector = target.getViewVector(1.0F);
                Vec3 vec31 = vec32.vectorTo(target.position()).normalize();
                vec31 = new Vec3(vec31.x, 0.0D, vec31.z);
                if (vec31.dot(targetViewVector) < 0.0D) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void onHitBlock(BlockHitResult raytrace) {
        super.onHitBlock(raytrace);
        if(this.level().isClientSide())
        {
            this.level().addParticle(getParticle(), this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            return;
        }
    }

    /** EVENTS **/



    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.level().addParticle(getParticle(), this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }
}
