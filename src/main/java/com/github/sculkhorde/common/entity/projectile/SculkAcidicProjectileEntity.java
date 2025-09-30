package com.github.sculkhorde.common.entity.projectile;

import com.github.sculkhorde.common.effect.CorrodingEffect;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModItems;
import com.github.sculkhorde.util.DifficultyUtil;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.ProjectileUtil;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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

    public SculkAcidicProjectileEntity(Level worldIn) {
        this(ModEntities.SCULK_ACIDIC_PROJECTILE_ENTITY.get(), worldIn);
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

            if(ProjectileUtil.isEntityBlockingProjectile(target, this))
            {
                return;
            }

            if(DifficultyUtil.isCurrentDifficultyEasy())
            {
                EntityAlgorithms.doSculkTypeDamageToEntity(owner, target, 6F, 5F);
            }
            else if(DifficultyUtil.isCurrentDifficultyNormal())
            {
                if(CorrodingEffect.isEntityAffectableByCorroded(target))
                {
                    CorrodingEffect.applyToEntity(owner, target, TickUnits.convertSecondsToTicks(3));
                }
                else
                {
                    EntityAlgorithms.doSculkTypeDamageToEntity(owner, target, 6F, 2F);
                }
            }
            else if(DifficultyUtil.isCurrentDifficultyHard())
            {
                if(CorrodingEffect.isEntityAffectableByCorroded(target))
                {
                    CorrodingEffect.applyToEntity(owner, target, TickUnits.convertSecondsToTicks(5));
                }
                else
                {
                    EntityAlgorithms.doSculkTypeDamageToEntity(owner, target, 6F, 5F);
                }
            }

        }
    }


    @Override
    protected void onHitBlock(BlockHitResult raytrace) {
        super.onHitBlock(raytrace);
        if(this.level().isClientSide())
        {
            this.level().addParticle(getParticle(), this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            return;
        }

        if (random.nextFloat() < 0.028F && !(getOwner() instanceof Player && ((Player) getOwner()).isCreative()))
        {
            final Vec3 vec = raytrace.getLocation();
            final ItemEntity item = new ItemEntity(this.level(), vec.x, vec.y + 0.25D, vec.z, new ItemStack(getDefaultItem()));
            this.level().addFreshEntity(item);
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
