package com.github.sculkhorde.common.entity.projectile;

import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModItems;
import com.github.sculkhorde.util.EntityAlgorithms;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.network.NetworkHooks;

/**
 * This class is mainly used as a parent class and is used for
 * projectile entities.
 */
public class CustomItemProjectileEntity extends ThrowableItemProjectile {

    private float damage = 50f;

    /** CONSTRUCTORS **/

    /**
     * Default Constructor
     * @param entityIn The Entity we are Shooting
     * @param worldIn The world the projectile will exist in
     */
    public CustomItemProjectileEntity(EntityType<? extends CustomItemProjectileEntity> entityIn, Level worldIn) {

        super(entityIn, worldIn);

    }

    /**
     * Private Constructor
     * @param worldIn The World
     * @param shooterIn The Entity Shooting the Projectile
     */
    public CustomItemProjectileEntity(Level worldIn, LivingEntity shooterIn, float damageIn) {
        this(ModEntities.CUSTOM_ITEM_PROJECTILE_ENTITY.get(), worldIn);
        this.setPos(shooterIn.getX(), shooterIn.getEyeY(), shooterIn.getZ());
        this.setOwner(shooterIn);
        this.setDamage(damageIn);
    }


    /** MODIFIERS **/

    /**
     * Sets how much damage the projectile does
     * @param damageIn The damage amount
     * @return This projectile
     */
    public CustomItemProjectileEntity setDamage(float damageIn)
    {
        this.damage = damageIn;
        return this;
    }

    /** ACCESSORS **/

    /**
     * Determines what item represents this entity
     * @return The Item
     */
    protected Item getDefaultItem() {
        return ModItems.CUSTOM_ITEM_PROJECTILE.get();
    }

    /**
     * Used to determine what partciles this entity should create when it hits a wall.
     * @return The Particle Data
     */
    protected ParticleOptions getParticle()
    {
        return new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(getDefaultItem()));
    }

    /**
     * Needed for spawning.
     * @return The Packet
     */
    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    /** EVENTS **/

    /**
     * Gets called every game tick the projectile exist.
     */
    @Override
    public void tick()
    {
        Entity entity = getOwner();
        //If the owner is a player that is dead, remove the projectile.
        //Else, proceed as normal.
        if (entity instanceof net.minecraft.world.entity.player.Player && !entity.isAlive()) {
            remove(RemovalReason.DISCARDED);
        } else {
            super.tick();
        }
    }

    /**
     * Gets called when this projectile hits an entity.
     * @param raytrace The resulting raytrace object that contains the context
     */
    @Override
    protected void onHitEntity(EntityHitResult raytrace)
    {
        super.onHitEntity(raytrace);

        // This is a safety check to make sure the entity is a living entity.
        // Mutant mobs previously caused a crash related to this, though
        // I'm confident that this is an oversight on my part.
        if(!(raytrace.getEntity() instanceof LivingEntity))
        {
           return;
        }

        // If the entity is a sculk or if the entity it hit was the owner, do nothing.
        if(EntityAlgorithms.isSculkLivingEntity.test((LivingEntity) raytrace.getEntity()) || getOwner() == raytrace.getEntity())
        {
            return;
        }


        raytrace.getEntity().hurt(DamageSource.thrown(this, getOwner()), damage);
        this.playSound(SoundEvents.HONEY_BLOCK_BREAK, 1.0F, 1.0F + random.nextFloat() * 0.2F);

        if(raytrace.getEntity() instanceof LivingEntity)
        {
            //((LivingEntity)raytrace.getEntity()).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 5, 1));
        }

        remove(RemovalReason.DISCARDED);

    }

    /**
     * Gets called whenever the projectile hits a block.
     * Has a chance to drop the item it represents.
     * @param raytrace The context
     */
    @Override
    protected void onHitBlock(BlockHitResult raytrace)
    {
        super.onHitBlock(raytrace);
        if (random.nextFloat() < 0.028F && !(getOwner() instanceof Player && ((Player) getOwner()).isCreative()))
        {
            final Vec3 vec = raytrace.getLocation();
            final ItemEntity item = new ItemEntity(this.level, vec.x, vec.y + 0.25D, vec.z, new ItemStack(getDefaultItem()));
            this.level.addFreshEntity(item);
        }
        else
        {
            this.playSound(SoundEvents.HONEY_BLOCK_BREAK, 1.0F, 1.0F + random.nextFloat() * 0.2F);
        }
        this.remove(RemovalReason.DISCARDED);
    }

    /**
     * Do not know why I need this; comes from
     * https://github.com/skyjay1/GreekFantasy/blob/master/src/main/java/greekfantasy/entity/misc/DiscusEntity.java
     * @param serverWorld The Server World
     * @param iTeleporter The teleporter object
     * @return ???
     */
    @Override
    public Entity changeDimension(ServerLevel serverWorld, ITeleporter iTeleporter)
    {
        Entity entity = this.getOwner();
        if (entity != null && entity.level.dimension() != serverWorld.dimension())
        {
            setOwner(null);
        }
        return super.changeDimension(serverWorld, iTeleporter);
    }


    /**
     * Handles an entity event fired from net.minecraft.world.level.Level#broadcastEntityEvent.
     */
    @OnlyIn(Dist.CLIENT)
    public void handleEntityEvent(byte pId) {
        if (pId == 3) {
            ParticleOptions iparticledata = this.getParticle();

            for(int i = 0; i < 8; ++i) {
                this.level.addParticle(iparticledata, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }

    }

}
