package com.github.sculkhoard.common.entity.projectile;

import com.github.sculkhoard.common.entity.SculkLivingEntity;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class AcidBallEntity extends SmallFireballEntity {

    private float damage = 6.0F; //The default amount of damage this does. 1 Heart is 2 points of damage.

    /**
     * The Constructor
     * @param worldIn The World
     * @param shooter The Entity Who is Shooting
     * @param accelX The x direction it should be going
     * @param accelY The y direction it should be going
     * @param accelZ The z direction it should be going
     * @param damage The amount of damage
     */
    public AcidBallEntity(World worldIn, LivingEntity shooter, double accelX, double accelY, double accelZ,
                          float damage) {
        super(worldIn, shooter, accelX, accelY, accelZ);
        this.damage = damage;
    }


    /**
     * Called when this Entity hits a block or entity.
     */
    @Override
    protected void onHit(RayTraceResult pResult) {
        super.onHit(pResult);
        if (!this.level.isClientSide)
        {
            this.remove();
        }
    }

    /**
     * Do damage to the entity that the projectile hits
     * @param rayTraceResult The Resulting Raytrace
     */
    @Override
    protected void onHitEntity(EntityRayTraceResult rayTraceResult) {
        if (!this.level.isClientSide) {
            Entity targetEntity = rayTraceResult.getEntity();
            Entity ownerEntity = this.getOwner();
            //TODO: Change the damage source of Acid Ball
            //No friendly fire
            if(!(targetEntity instanceof SculkLivingEntity))
                targetEntity.hurt(DamageSource.fireball(this, ownerEntity), damage);

            //TODO: Figure out what doEnchantDamageEffects does
            //If owner is a living entity, ???
            if (ownerEntity instanceof LivingEntity) {
                this.doEnchantDamageEffects((LivingEntity) ownerEntity, ownerEntity);
            }

        }
    }

    @Override
    protected void onHitBlock(BlockRayTraceResult pResult) {
        super.onHitBlock(pResult);
        if (!this.level.isClientSide) {
            Entity entity = this.getOwner();
            if (entity == null || !(entity instanceof MobEntity) || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this.getEntity())) {
                BlockPos blockpos = pResult.getBlockPos().relative(pResult.getDirection());
                if (this.level.isEmptyBlock(blockpos)) {
                    //this.level.setBlockAndUpdate(blockpos, AbstractFireBlock.getState(this.level, blockpos));
                }
            }

        }
    }
}
