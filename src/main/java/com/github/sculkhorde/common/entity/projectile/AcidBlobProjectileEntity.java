package com.github.sculkhorde.common.entity.projectile;

import com.github.sculkhorde.common.entity.AreaEffectSphericalCloudEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.util.ColorUtil;
import com.github.sculkhorde.util.ParticleUtil;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;


public class AcidBlobProjectileEntity extends AbstractProjectileEntity implements GeoEntity {

    /** CONSTRUCTORS **/

    /**
     * Default Constructor
     * @param entityIn The Entity we are Shooting
     * @param worldIn The world the projectile will exist in
     */
    public AcidBlobProjectileEntity(EntityType<? extends Projectile> entityIn, Level worldIn) {
        super(entityIn, worldIn);
    }

    @Override
    protected void applyEffectToEntity(LivingEntity entity) {

    }

    public AcidBlobProjectileEntity(Level level, LivingEntity shooter, float damage)
    {
        this(ModEntities.ACID_BLOB_PROJECTILE_ENTITY.get(), level);
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
        AreaEffectSphericalCloudEntity effectCloud = new AreaEffectSphericalCloudEntity(level(), getX(), getY(), getZ());
        if(getOwner() instanceof LivingEntity livingOwner) { effectCloud.setOwner(livingOwner); }
        effectCloud.setRadius(3.0F);
        effectCloud.setDuration(TickUnits.convertSecondsToTicks(5));
        effectCloud.addEffect(new MobEffectInstance(ModMobEffects.CORRODED, TickUnits.convertSecondsToTicks(30), 1));
        level().addFreshEntity(effectCloud);
        discard();
    }

    @Override
    public void trailParticles() {
        float spawnX = (float) (getX() + level().getRandom().nextFloat());
        float spawnY = (float) (getY() + level().getRandom().nextFloat());
        float spawnZ = (float) (getZ() + level().getRandom().nextFloat());
        ParticleUtil.spawnColoredDustParticleOnClient((ClientLevel) level(),
                ColorUtil.getRandomHexAcidColor(this.random),
                0.8F,
                new Vector3f(spawnX, spawnY, spawnZ),
                new Vector3f(0, this.random.nextFloat() * - 1, 0));
    }

    @Override
    public void impactParticles(double x, double y, double z) {

    }

    @Override
    public float getSpeed() {
        return 1.75F;
    }

    @Override
    public Optional<SoundEvent> getImpactSound() {
        return Optional.of(SoundEvents.FIREWORK_ROCKET_BLAST);
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
