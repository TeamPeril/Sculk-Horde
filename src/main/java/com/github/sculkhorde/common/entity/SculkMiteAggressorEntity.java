package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.goal.DespawnWhenIdle;
import com.github.sculkhorde.common.entity.goal.InvalidateTargetGoal;
import com.github.sculkhorde.common.entity.goal.NearestLivingEntityTargetGoal;
import com.github.sculkhorde.common.entity.goal.SculkMiteAggressorAttackGoal;
import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.util.TargetParameters;
import net.minecraft.block.BlockState;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Random;

public class SculkMiteAggressorEntity extends SculkLivingEntity implements IAnimatable, ISculkSmartEntity {

    /**
     * In order to create a mob, the following files were created/edited.<br>
     * Edited core/ EntityRegistry.java<br>
     * Edited util/ ModEventSubscriber.java<br>
     * Edited client/ ClientModEventSubscriber.java<br>
     * Edited common/world/gen/ModEntityGen.java<br>
     * Added common/entity/ SculkMiteAggressor.java<br>
     * Added client/model/entity/ SculkMiteAggressorModel.java<br>
     * Added client/renderer/entity/ SculkMiteAggressorRenderer.java
     */

    //The Health
    public static final float MAX_HEALTH = 5F;
    //The armor of the mob
    public static final float ARMOR = 2F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 2F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 1F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 25F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.3F;

    // Controls what types of entities this mob can target
    private TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetHostiles().enableMustReachTarget();

    private AnimationFactory factory = new AnimationFactory(this);

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkMiteAggressorEntity(EntityType<? extends SculkMiteAggressorEntity> type, World worldIn) {
        super(type, worldIn);
    }

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public SculkMiteAggressorEntity(World worldIn) {super(EntityRegistry.SCULK_MITE_AGGRESSOR, worldIn);}

    /**
     * Determines & registers the attributes of the mob.
     * @return The Attributes
     */
    public static AttributeModifierMap.MutableAttribute createAttributes()
    {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, ARMOR)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.ATTACK_KNOCKBACK, ATTACK_KNOCKBACK)
                .add(Attributes.FOLLOW_RANGE,FOLLOW_RANGE)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED);
    }

    public boolean isIdle() {
        return getTarget() == null;
    }

    @Override
    public TargetParameters getTargetParameters() {
        return TARGET_PARAMETERS;
    }

    /**
     * The function that determines if a position is a good spawn location<br>
     * @param config ???
     * @param world The world that the mob is trying to spawn in
     * @param reason An object that indicates why a mob is being spawned
     * @param pos The Block Position of the potential spawn location
     * @param random ???
     * @return Returns a boolean determining if it is a suitable spawn location
     */
    public static boolean passSpawnCondition(EntityType<? extends CreatureEntity> config, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
        // If peaceful, return false
        if (world.getDifficulty() == Difficulty.PEACEFUL) return false;
            // If not because of chunk generation or natural, return false
        else if (reason != SpawnReason.CHUNK_GENERATION && reason != SpawnReason.NATURAL) return false;
            //If block below is not sculk crust, return false
        else if (world.getBlockState(pos.below()).getBlock() != BlockRegistry.CRUST.get()) return false;
        return true;
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
                        new DespawnWhenIdle(this, 120),
                        //SwimGoal(mob)
                        new SwimGoal(this),
                        //MeleeAttackGoal(mob, speedModifier, followingTargetEvenIfNotSeen)
                        new SculkMiteAggressorAttackGoal(this, 1.0D, true),
                        //MoveTowardsTargetGoal(mob, speedModifier, within) THIS IS FOR NON-ATTACKING GOALS
                        new MoveTowardsTargetGoal(this, 0.8F, 20F),
                        //WaterAvoidingRandomWalkingGoal(mob, speedModifier)
                        new WaterAvoidingRandomWalkingGoal(this, 1.0D),
                        //LookAtGoal(mob, targetType, lookDistance)
                        new LookAtGoal(this, PigEntity.class, 8.0F),
                        //LookRandomlyGoal(mob)
                        new LookRandomlyGoal(this)
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
                        new InvalidateTargetGoal(this),
                        //HurtByTargetGoal(mob)
                        new HurtByTargetGoal(this).setAlertOthers(),
                        new NearestLivingEntityTargetGoal<>(this, true, true)
                };
        return goals;
    }


    //Animation Stuff below

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event)
    {
        //event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.bat.fly", true));
        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.SILVERFISH_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.SILVERFISH_DEATH;
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(SoundEvents.SILVERFISH_STEP, 0.15F, 1.0F);
    }
}
