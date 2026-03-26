package com.github.sculkhorde.common.entity.boss.angel_of_reaping;

import com.github.sculkhorde.common.entity.boss.SpecialEffectEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.ParticleUtil;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class ElementalFireMagicCircleAttackEntity extends SpecialEffectEntity implements GeoEntity {
    public static int LIFE_TIME = TickUnits.convertSecondsToTicks(5);
    public int currentLifeTicks = 0;

    protected float DAMAGE = 5F;
    protected int attack_delay_ticks = 0;

    public ElementalFireMagicCircleAttackEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        triggerAnim(ATTACK_CONTROLLER_ANIMATION_ID, HIDE_ANIMATION_ID);
    }

    public ElementalFireMagicCircleAttackEntity(Level level) {
        this(ModEntities.ELEMENTAL_FIRE_MAGIC_CIRCLE.get(), level);
    }

    public ElementalFireMagicCircleAttackEntity(Level level, double x, double y, double z, float angle, LivingEntity owner) {
        this(level);
        setPos(x,y,z);
        this.setYRot(angle * (180F / (float)Math.PI));
        setOwner(owner);
    }

    public void setDelay(int value)
    {
        attack_delay_ticks = value;
    }

    protected void applyEffect(LivingEntity entity)
    {
        float finalDamage = DAMAGE + (EntityAlgorithms.getStrengthOfLivingEntity(getOwner()) * 2);
        boolean didHurt = entity.hurt(damageSources().magic(), finalDamage);

        if(!didHurt)
        {
            return;
        }

        entity.setSecondsOnFire(10);

        if(getOwner() != null)
        {
            entity.hurt(getOwner().damageSources().magic(), finalDamage);
        }
        else
        {
            entity.hurt(damageSources().magic(), finalDamage);
        }

    }

    protected void doAreaOfEffectAttack()
    {
        AABB hitbox = getBoundingBox().inflate(0,5,0);

        List<LivingEntity> damageHitList = EntityAlgorithms.getEntitiesExceptOwnerInBoundingBox(getOwner(), (ServerLevel) level(), hitbox);

        for (LivingEntity entity : damageHitList)
        {
            if (getOwner() != null && getOwner().equals(entity)) {
                continue;
            }

            applyEffect(entity);
        }
    }

    public void spawnPartilcesRandomlyInHitboxClientSide() {
        AABB boundingBox = getBoundingBox();
        float spawnX = (float) (boundingBox.minX + (boundingBox.maxX - boundingBox.minX) * level().getRandom().nextFloat());
        float spawnY = (float) (boundingBox.minY + (boundingBox.maxY - boundingBox.minY) * level().getRandom().nextFloat());
        float spawnZ = (float) (boundingBox.minZ + (boundingBox.maxZ - boundingBox.minZ) * level().getRandom().nextFloat());

        Vector3f spawn = new Vector3f(spawnX, spawnY, spawnZ);
        Vector3f deltaMovement = new Vector3f(0, 1, 0);

        ParticleUtil.spawnFlameParticleOnClient((ClientLevel) level(), spawn, deltaMovement);
    }

    @Override
    public void tick() {
        super.tick();

        if(level().isClientSide()) {
            spawnPartilcesRandomlyInHitboxClientSide();
            return;
        }

        currentLifeTicks++;

        // If the entity is alive for more than LIFE_TIME, discard it
        if((currentLifeTicks >= LIFE_TIME || ATTACK_ANIMATION_CONTROLLER.hasAnimationFinished()) && LIFE_TIME != -1)
        {
            this.discard();
        }

        if(this.currentLifeTicks == attack_delay_ticks)
        {
            doAreaOfEffectAttack();
            triggerAnim(ATTACK_CONTROLLER_ANIMATION_ID, ATTACK_ANIMATION_ID);
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.EVOKER_CAST_SPELL, this.getSoundSource(), 1.0F, this.random.nextFloat() * 0.2F + 0.85F, false);
        }

    }

    // #### Animation Code ####
    protected static final String ATTACK_CONTROLLER_ANIMATION_ID = "attack_controller";
    protected static final String ATTACK_ANIMATION_ID = "attack";
    protected static final RawAnimation ATTACK_ANIMATION = RawAnimation.begin().thenPlayAndHold(ATTACK_ANIMATION_ID);
    protected static final String HIDE_ANIMATION_ID = "hide";
    protected static final RawAnimation HIDE_ANIMATION = RawAnimation.begin().thenPlayAndHold(HIDE_ANIMATION_ID);

    protected final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    protected final AnimationController ATTACK_ANIMATION_CONTROLLER = new AnimationController<>(this, ATTACK_CONTROLLER_ANIMATION_ID, state -> PlayState.STOP)
            .triggerableAnim(ATTACK_ANIMATION_ID, ATTACK_ANIMATION)
            .triggerableAnim(HIDE_ANIMATION_ID, HIDE_ANIMATION);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(ATTACK_ANIMATION_CONTROLLER);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
