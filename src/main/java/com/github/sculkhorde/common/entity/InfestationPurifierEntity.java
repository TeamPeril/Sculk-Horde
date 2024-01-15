package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.infection.CursorSurfacePurifierEntity;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModItems;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;

public class InfestationPurifierEntity extends PathfinderMob implements GeoEntity {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited core/ EntityRegistry.java<br>
     * Edited util/ ModEventSubscriber.java<br>
     * Edited client/ ClientModEventSubscriber.java<br>
     * Added common/entity/ InfestationPurifierEntity.java<br>
     * Added client/model/entity/ InfestationPurifierEntity.java<br>
     * Added client/renderer/entity/ InfestationPurifierEntity.java
     */

    //The Health
    public static final float MAX_HEALTH = 200F;
    //The armor of the mob
    public static final float ARMOR = 10F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 0F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 0F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 0F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0F;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    CursorSurfacePurifierEntity cursor1;
    CursorSurfacePurifierEntity cursor2;
    CursorSurfacePurifierEntity cursor3;
    CursorSurfacePurifierEntity cursor4;


    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public InfestationPurifierEntity(EntityType<? extends InfestationPurifierEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public InfestationPurifierEntity(Level worldIn) {super(ModEntities.INFESTATION_PURIFIER.get(), worldIn);}

    /**
     * Determines & registers the attributes of the mob.
     * @return The Attributes
     */
    public static AttributeSupplier.Builder createAttributes()
    {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, ARMOR)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.ATTACK_KNOCKBACK, ATTACK_KNOCKBACK)
                .add(Attributes.FOLLOW_RANGE,FOLLOW_RANGE)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    /**
     * Registers Goals with the entity. The goals determine how an AI behaves ingame.
     * Each goal has a priority with 0 being the highest and as the value increases, the priority is lower.
     * You can manually add in goals in this function, however, I made an automatic system for this.
     */
    @Override
    public void registerGoals() {

        Goal[] goalSelectorPayload = goalSelectorPayload();
        for(int priority = 0; priority < goalSelectorPayload.length; priority++)
        {
            this.goalSelector.addGoal(priority, goalSelectorPayload[priority]);
        }

        Goal[] targetSelectorPayload = targetSelectorPayload();
        for(int priority = 0; priority < targetSelectorPayload.length; priority++)
        {
            this.targetSelector.addGoal(priority, targetSelectorPayload[priority]);
        }

    }

    /**
     * Prepares an array of goals to give to registerGoals() for the goalSelector.<br>
     * The purpose was to make registering goals simpler by automatically determining priority
     * based on the order of the items in the array. First element is of priority 0, which
     * represents highest priority. Priority value then increases by 1, making each element
     * less of a priority than the last.
     * @return Returns an array of goals ordered from highest to lowest piority
     */
    public Goal[] goalSelectorPayload()
    {
        Goal[] goals =
                {
                        // MeleeAttackGoal(mob, speedModifier, followingTargetEvenIfNotSeen)
                        //new dieAfterTimeGoal(this),
                };
        return goals;
    }

    /**
     * Prepares an array of goals to give to registerGoals() for the targetSelector.<br>
     * The purpose was to make registering goals simpler by automatically determining priority
     * based on the order of the items in the array. First element is of priority 0, which
     * represents highest priority. Priority value then increases by 1, making each element
     * less of a priority than the last.
     * @return Returns an array of goals ordered from highest to lowest piority
     */
    public Goal[] targetSelectorPayload()
    {
        Goal[] goals =
                {
                        //HurtByTargetGoal(mob)
                        //new TargetAttacker(this).setAlertAllies(),
                };
        return goals;
    }

    @Override
    public void checkDespawn() {} // Do nothing because we do not want this mob to despawn

    // Add our animations
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericLivingController(this));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    //Every tick, spawn a short range cursor
    @Override
    public void aiStep()
    {
        super.aiStep();

        // Only on the client side, spawn dust particles with a specific color
        // Have the partciles fly in random directions
        if (level().isClientSide)
        {
            return;
        }

        Random random = new Random();
        if (random.nextInt(100) != 0) { return; }

        tellServerToSpawnCursors();

        // Any sculk entity within 10 blocks of the spewer will be set on fire
        ArrayList<LivingEntity> entities = (ArrayList<LivingEntity>) EntityAlgorithms.getLivingEntitiesInBoundingBox((ServerLevel) level(), this.getBoundingBox().inflate(10));
        for (LivingEntity entity : entities)
        {
            if (entity != null && EntityAlgorithms.isSculkLivingEntity.test(entity))
            {
                // Set entity on fire
                //entity.setSecondsOnFire(60);
                // Give entity potion effects
                //entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
                //entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 3));
                //entity.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 3));
            }
        }

    }

    protected void tellServerToSpawnCursors()
    {
        if(level().isClientSide()) { return; }

        /*
        level().getServer().tell(new net.minecraft.server.TickTask(level().getServer().getTickCount() + 1, () -> {
            if((cursor1 == null || !cursor1.isAlive() ))
            {
                // Spawn Block Traverser
                cursor1 = new CursorSurfacePurifierEntity(level());
                cursor1.setPos(this.blockPosition().getX(), this.blockPosition().getY() - 1, this.blockPosition().getZ());
                cursor1.setMaxTransformations(100);
                cursor1.setMaxRange(ModConfig.SERVER.infestation_purifier_range.get());
                cursor1.setSearchIterationsPerTick(2);
                cursor1.setMaxLifeTimeMillis(TimeUnit.MINUTES.toMillis(10));
                cursor1.setTickIntervalMilliseconds(20);
                level().addFreshEntity(cursor1);
            }

            if((cursor2 == null || !cursor2.isAlive() ))
            {
                // Spawn Block Traverser
                cursor2 = new CursorSurfacePurifierEntity(level());
                cursor2.setPos(this.blockPosition().getX(), this.blockPosition().getY() - 1, this.blockPosition().getZ());
                cursor2.setMaxTransformations(100);
                cursor2.setMaxRange(ModConfig.SERVER.infestation_purifier_range.get());
                cursor2.setSearchIterationsPerTick(2);
                cursor2.setMaxLifeTimeMillis(TimeUnit.MINUTES.toMillis(10));
                cursor2.setTickIntervalMilliseconds(20);
                level().addFreshEntity(cursor2);
            }

            if((cursor3 == null || !cursor3.isAlive() ))
            {
                // Spawn Block Traverser
                cursor3 = new CursorSurfacePurifierEntity(level());
                cursor3.setPos(this.blockPosition().getX(), this.blockPosition().getY() - 1, this.blockPosition().getZ());
                cursor3.setMaxTransformations(100);
                cursor3.setMaxRange(ModConfig.SERVER.infestation_purifier_range.get());
                cursor3.setSearchIterationsPerTick(2);
                cursor3.setMaxLifeTimeMillis(TimeUnit.MINUTES.toMillis(10));
                cursor3.setTickIntervalMilliseconds(20);
                level().addFreshEntity(cursor3);
            }

            if((cursor4 == null || !cursor4.isAlive() ))
            {
                // Spawn Block Traverser
                cursor4 = new CursorSurfacePurifierEntity(level());
                cursor4.setPos(this.blockPosition().getX(), this.blockPosition().getY() - 1, this.blockPosition().getZ());
                cursor4.setMaxTransformations(100);
                cursor4.setMaxRange(ModConfig.SERVER.infestation_purifier_range.get());
                cursor4.setSearchIterationsPerTick(2);
                cursor4.setMaxLifeTimeMillis(TimeUnit.MINUTES.toMillis(10));
                cursor4.setTickIntervalMilliseconds(20);
                level().addFreshEntity(cursor4);
            }
        }));

         */
    }

    //If entity is rightclicked, drop item
    @Override
    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        if (!this.level().isClientSide) {
            this.remove(RemovalReason.DISCARDED);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemovedFromWorld() {
        if(level().isClientSide) { return; }
        //this.spawnAtLocation(new ItemStack(ModItems.INFESTATION_PURIFIER.get()));
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.BEACON_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.CONDUIT_ATTACK_TARGET;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.CONDUIT_DEACTIVATE;
    }

}
