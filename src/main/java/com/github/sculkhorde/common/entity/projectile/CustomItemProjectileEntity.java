package com.github.sculkhorde.common.entity.projectile;

import com.github.sculkhorde.common.entity.SculkLivingEntity;
import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.core.ItemRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * This class is mainly used as a parent class and is used for
 * projectile entities.
 */
public class CustomItemProjectileEntity extends ProjectileItemEntity {

    private float damage = 50f;

    /** CONSTRUCTORS **/

    /**
     * Default Constructor
     * @param entityIn The Entity we are Shooting
     * @param worldIn The world the projectile will exist in
     */
    public CustomItemProjectileEntity(EntityType<? extends CustomItemProjectileEntity> entityIn, World worldIn) {

        super(entityIn, worldIn);

    }

    /**
     * Private Constructor
     * @param worldIn The World
     * @param shooterIn The Entity Shooting the Projectile
     */
    public CustomItemProjectileEntity(World worldIn, LivingEntity shooterIn, float damageIn) {
        this(EntityRegistry.CUSTOM_ITEM_PROJECTILE_ENTITY, worldIn);
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
        return ItemRegistry.CUSTOM_ITEM_PROJECTILE.get();
    }

    /**
     * Used to determine what partciles this entity should create when it hits a wall.
     * @return The Particle Data
     */
    @OnlyIn(Dist.CLIENT)
    private IParticleData getParticle() {
        ItemStack itemstack = this.getItemRaw();
        return (IParticleData)(itemstack.isEmpty() ? ParticleTypes.NAUTILUS : new ItemParticleData(ParticleTypes.ITEM, itemstack));
    }

    /**
     * Don't know why we need this. Got it from https://github.com/skyjay1/GreekFantasy/blob/master/src/main/java/greekfantasy/entity/misc/DiscusEntity.java
     * @return ???
     */
    @Override
    public IPacket<?> getAddEntityPacket() {
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
        if (entity instanceof net.minecraft.entity.player.PlayerEntity && !entity.isAlive()) {
            remove();
        } else {
            super.tick();
        }
    }

    /**
     * Gets called when this projectile hits an entity.
     * @param raytrace The resulting raytrace object that contains the context
     */
    @Override
    protected void onHitEntity(EntityRayTraceResult raytrace)
    {
        super.onHitEntity(raytrace);
        //If the entity is not sculk or if the entity it hit was not the owner, do damage.
        if(! (raytrace.getEntity() instanceof SculkLivingEntity) && getOwner() != getEntity() )
        {
            raytrace.getEntity().hurt(DamageSource.thrown(this, getOwner()), damage);
            this.level.broadcastEntityEvent(this, (byte)3); //Create particle event (from SnowballEntity.java)
            remove();
        }
    }

    /**
     * Gets called whenever the projectile hits a block.
     * Has a chance to drop the item it represents.
     * @param raytrace The context
     */
    @Override
    protected void onHitBlock(BlockRayTraceResult raytrace)
    {
        super.onHitBlock(raytrace);
        if (random.nextFloat() < 0.028F && !(getOwner() instanceof PlayerEntity && ((PlayerEntity) getOwner()).isCreative()))
        {
            final Vector3d vec = raytrace.getLocation();
            final ItemEntity item = new ItemEntity(this.level, vec.x, vec.y + 0.25D, vec.z, new ItemStack(getDefaultItem()));
            this.level.addFreshEntity(item);
        }
        else
        {
            this.playSound(SoundEvents.ITEM_BREAK, 1.0F, 1.0F + random.nextFloat() * 0.2F);
        }
        this.level.broadcastEntityEvent(this, (byte)3); //Create Particle Effect
        this.remove();
    }

    /**
     * Do not know why I need this; comes from
     * https://github.com/skyjay1/GreekFantasy/blob/master/src/main/java/greekfantasy/entity/misc/DiscusEntity.java
     * @param serverWorld The Server World
     * @param iTeleporter The teleporter object
     * @return ???
     */
    @Override
    public Entity changeDimension(ServerWorld serverWorld, ITeleporter iTeleporter)
    {
        Entity entity = this.getOwner();
        if (entity != null && entity.level.dimension() != serverWorld.dimension())
        {
            setOwner(null);
        }
        return super.changeDimension(serverWorld, iTeleporter);
    }


    /**
     * Handles an entity event fired from {@link net.minecraft.world.level.Level#broadcastEntityEvent}.
     */
    @OnlyIn(Dist.CLIENT)
    public void handleEntityEvent(byte pId) {
        if (pId == 3) {
            IParticleData iparticledata = this.getParticle();

            for(int i = 0; i < 8; ++i) {
                this.level.addParticle(iparticledata, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }

    }

}
