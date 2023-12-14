package com.github.sculkhorde.common.entity.projectile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import com.github.sculkhorde.common.entity.infection.CursorSurfacePurifierEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModItems;
import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;


public class PurificationFlaskProjectileEntity extends CustomItemProjectileEntity {

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
        this(ModEntities.PURIFICATION_FLASK_PROJECTILE_ENTITY.get(), worldIn);
        this.setPos(shooterIn.getX(), shooterIn.getEyeY(), shooterIn.getZ());
        this.setOwner(shooterIn);
        this.setDamage(damageIn);
    }


    /** MODIFIERS **/

    /** ACCESSORS **/

    protected Item getDefaultItem() {
        return ModItems.PURITY_SPLASH_POTION.get();
    }


    /** EVENTS **/

    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            this.level.addParticle(ParticleTypes.COMPOSTER, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }


    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        // If any entities are close to the impact, remove the infection from them.
        for(LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(4.0D)))
        {
            entity.addEffect(new MobEffectInstance(ModMobEffects.PURITY.get(), TickUnits.convertMinutesToTicks(15)));
        }

        this.playSound(SoundEvents.SPLASH_POTION_BREAK, 1.0F, 1.0F + random.nextFloat() * 0.2F);
        //this.level.broadcastEntityEvent(this, (byte)3); //Create Particle Effect
        this.remove(RemovalReason.DISCARDED);

        ArrayList<BlockPos> list = BlockAlgorithms.getBlockPosInCircle(BlockPos.containing(result.getLocation()), 3, true);
        Collections.shuffle(list);
        list.removeIf(pos -> !level.getBlockState(pos).isSolidRender(level, pos));

        for(int i = 0; i < 5 && i < list.size(); i++)
        {
            CursorSurfacePurifierEntity cursor = new CursorSurfacePurifierEntity(level);
            // Spawn Infestation Purifier Cursors
            // Spawn Block Traverser
            cursor.setPos(list.get(i).getX(), list.get(i).getY(), list.get(i).getZ());
            cursor.setMaxTransformations(200);
            cursor.setMaxRange(100);
            cursor.setSearchIterationsPerTick(5);
            cursor.setMaxLifeTimeMillis(TimeUnit.MINUTES.toMillis(1));
            cursor.setTickIntervalMilliseconds(150);
            level.addFreshEntity(cursor);

        }
    }
}
