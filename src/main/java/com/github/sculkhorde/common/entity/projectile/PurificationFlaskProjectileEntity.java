package com.github.sculkhorde.common.entity.projectile;

import com.github.sculkhorde.common.entity.infection.CursorSurfacePurifierEntity;
import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.core.ItemRegistry;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.EntityAlgorithms;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;


public class PurificationFlaskProjectileEntity extends CustomItemProjectileEntity {

    private float damage = 50f;

    /** CONSTRUCTORS **/

    /**
     * Default Constructor
     * @param entityIn The Entity we are Shooting
     * @param worldIn The world the projectile will exist in
     */
    public PurificationFlaskProjectileEntity(EntityType<? extends CustomItemProjectileEntity> entityIn, Level worldIn) {
        super(entityIn, worldIn);
    }

    /**
     * Constructor
     * @param worldIn The World to spawn the projectile in
     */
    public PurificationFlaskProjectileEntity(Level worldIn, LivingEntity shooterIn, float damageIn) {
        this(EntityRegistry.PURIFICATION_FLASK_PROJECTILE_ENTITY.get(), worldIn);
        this.setPos(shooterIn.getX(), shooterIn.getEyeY(), shooterIn.getZ());
        this.setOwner(shooterIn);
        this.setDamage(damageIn);
    }


    /** MODIFIERS **/

    /** ACCESSORS **/

    protected Item getDefaultItem() {
        return ItemRegistry.PURIFICATION_FLASK_ITEM.get();
    }


    /** EVENTS **/

    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            this.level.addParticle(ParticleTypes.COMPOSTER, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
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


        raytrace.getEntity().hurt(damageSources().thrown(this, getOwner()), damage);
        this.playSound(SoundEvents.SPLASH_POTION_BREAK, 1.0F, 1.0F + random.nextFloat() * 0.2F);
        this.level.broadcastEntityEvent(this, (byte)3); //Create particle event (from SnowballEntity.java)

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

        this.playSound(SoundEvents.SPLASH_POTION_BREAK, 1.0F, 1.0F + random.nextFloat() * 0.2F);
        //this.level.broadcastEntityEvent(this, (byte)3); //Create Particle Effect
        this.remove(RemovalReason.DISCARDED);

        ArrayList<BlockPos> list = BlockAlgorithms.getBlockPosInCircle(raytrace.getBlockPos(), 3, true);
        Collections.shuffle(list);
        list.removeIf(pos -> !level.getBlockState(pos).isSolidRender(level, pos));

        for(int i = 0; i < 5 && i < list.size(); i++)
        {
            CursorSurfacePurifierEntity cursor = new CursorSurfacePurifierEntity(level);
            // Spawn Infestation Purifier Cursors
            // Spawn Block Traverser
            cursor.setPos(list.get(i).getX(), list.get(i).getY(), list.get(i).getZ());
            cursor.setMaxInfections(5);
            cursor.setMaxRange(10);
            cursor.setSearchIterationsPerTick(5);
            cursor.setMaxLifeTimeMillis(TimeUnit.MINUTES.toMillis(1));
            cursor.setTickIntervalMilliseconds(150);
            level.addFreshEntity(cursor);

        }
    }
}
