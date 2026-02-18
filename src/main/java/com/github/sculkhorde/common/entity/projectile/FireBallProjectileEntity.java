package com.github.sculkhorde.common.entity.projectile;

import com.github.sculkhorde.common.entity.AreaEffectSphericalCloudEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.util.*;
import com.github.sculkhorde.util.hitboxes.HitboxUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;


public class FireBallProjectileEntity extends AbstractProjectileEntity implements GeoEntity {

    protected final int EXPLODE_RADIUS = 4;

    /** CONSTRUCTORS **/

    /**
     * Default Constructor
     * @param entityIn The Entity we are Shooting
     * @param worldIn The world the projectile will exist in
     */
    public FireBallProjectileEntity(EntityType<? extends Projectile> entityIn, Level worldIn) {
        super(entityIn, worldIn);
        setNoGravity(true);
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected void applyEffectToEntity(LivingEntity entity) {

    }

    public FireBallProjectileEntity(Level level, LivingEntity shooter, float damage)
    {
        this(ModEntities.FIRE_BALL_PROJECTILE_ENTITY.get(), level);
        setOwner(shooter);
        setDamage(damage);
    }


    /** MODIFIERS **/

    /** ACCESSORS **/

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        explode();
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        explode();
    }

    protected void explode()
    {
        if(level().isClientSide())
        {
            return;
        }

        AABB hitbox = HitboxUtil.createBoundingBoxCubeAtBlockPos(position(), EXPLODE_RADIUS * 2);
        List<LivingEntity> entitiesInHitBox = EntityAlgorithms.getEntitiesExceptOwnerInBoundingBox((LivingEntity) getOwner(), (ServerLevel) level(), hitbox);

        for(LivingEntity entity : entitiesInHitBox)
        {
            if(EntityAlgorithms.getDistanceBetweenEntities(this, entity) <= EXPLODE_RADIUS)
            {
                entity.hurt(damageSources().onFire(), getDamage());
                entity.setSecondsOnFire(5 + (5 * DifficultyUtil.getCurrentDifficulty().getId()));
            }
        }

        discard();
    }

    @Override
    public void trailParticles() {
        float spawnX = (float) (getX() + level().getRandom().nextFloat());
        float spawnY = (float) (getY() + level().getRandom().nextFloat());
        float spawnZ = (float) (getZ() + level().getRandom().nextFloat());
        ParticleUtil.spawnColoredDustParticleOnClient((ClientLevel) level(),
                ColorUtil.purityLightColor4,
                0.8F,
                new Vector3f(spawnX, spawnY, spawnZ),
                new Vector3f(0, this.random.nextFloat() * - 1, 0));
    }

    @Override
    public void impactParticles(double x, double y, double z) {

    }

    @Override
    public float getSpeed() {
        return 0.35F;
    }

    @Override
    public Optional<SoundEvent> getImpactSound() {
        return Optional.of(SoundEvents.GENERIC_EXPLODE);
    }


    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericLivingController(this));

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
