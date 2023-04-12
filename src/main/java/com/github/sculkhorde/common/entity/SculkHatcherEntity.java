package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.goal.DespawnWhenIdle;
import com.github.sculkhorde.common.entity.goal.InvalidateTargetGoal;
import com.github.sculkhorde.common.entity.goal.NearestLivingEntityTargetGoal;
import com.github.sculkhorde.common.entity.goal.TargetAttacker;
import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.util.TargetParameters;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Random;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;

public class SculkHatcherEntity extends SculkLivingEntity implements IAnimatable, ISculkSmartEntity {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited {@link EntityRegistry}<br>
     * Edited {@link com.github.sculkhorde.util.ModEventSubscriber}<br>
     * Edited {@link com.github.sculkhorde.client.ClientModEventSubscriber}<br>
     * Edited {@link com.github.sculkhorde.common.world.ModWorldEvents} (this might not be necessary)<br>
     * Edited {@link com.github.sculkhorde.common.world.gen.ModEntityGen}<br>
     * Added {@link SculkHatcherEntity}<br>
     * Added {@link com.github.sculkhorde.client.model.enitity.SculkHatcherModel} <br>
     * Added {@link com.github.sculkhorde.client.renderer.entity.SculkHatcherRenderer}
     */

    //The Health
    public static final float MAX_HEALTH = 20F;
    //The armor of the mob
    public static final float ARMOR = 4F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 3F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 1F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 25F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.25F;

    // Controls what types of entities this mob can target
    private TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetHostiles().ignoreTargetBelow50PercentHealth().enableMustReachTarget();

    /**
     * SPAWN_WEIGHT determines how likely a mob is to spawn. Bigger number = greater chance<br>
     * 100 = Zombie<br>
     * 12 = Sheep<br>
     * 10 = Enderman<br>
     * 8 = Cow<br>
     * 5 = Witch<br>
     */
    public static int SPAWN_WEIGHT = 100;
    /**
     * SPAWN_MIN determines the minimum amount of this mob that will spawn in a group<br>
     * SPAWN_MAX determines the maximum amount of this mob that will spawn in a group<br>
     * SPAWN_Y_MAX determines the Maximum height this mob can spawn<br>
     */
    public static int SPAWN_MIN = 1;
    public static int SPAWN_MAX = 3;
    public static int SPAWN_Y_MAX = 80;

    //factory The animation factory used for animations
    private AnimationFactory factory = new AnimationFactory(this);

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkHatcherEntity(EntityType<? extends SculkHatcherEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public SculkHatcherEntity(Level worldIn) {super(EntityRegistry.SCULK_HATCHER, worldIn);}

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
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED);
    }

    public boolean isIdle() {
        return getTarget() == null;
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.COW_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.COW_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.COW_DEATH;
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlockIn) {
        this.playSound(SoundEvents.COW_STEP, 0.15F, 1.0F);
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
    public static boolean passSpawnCondition(EntityType<? extends PathfinderMob> config, LevelAccessor world, MobSpawnType reason, BlockPos pos, Random random)
    {
        // If peaceful, return false
        if (world.getDifficulty() == Difficulty.PEACEFUL) return false;
        // If not because of chunk generation or natural, return false
        else if (reason != MobSpawnType.CHUNK_GENERATION && reason != MobSpawnType.NATURAL) return false;
        //If above SPAWN_Y_MAX and the block below is not sculk crust, return false
        else if (pos.getY() > SPAWN_Y_MAX && world.getBlockState(pos.below()).getBlock() != BlockRegistry.CRUST.get()) return false;
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
                        new DespawnWhenIdle(this, 30),
                        //SwimGoal(mob)
                        new FloatGoal(this),
                        //MeleeAttackGoal(mob, speedModifier, followingTargetEvenIfNotSeen)
                        new SculkHatcherAttackGoal(this, 1.0D, true),
                        //MoveTowardsTargetGoal(mob, speedModifier, within) THIS IS FOR NON-ATTACKING GOALS
                        new MoveTowardsTargetGoal(this, 0.8F, 20F),
                        //WaterAvoidingRandomWalkingGoal(mob, speedModifier)
                        new WaterAvoidingRandomStrollGoal(this, 1.0D),
                        //new RangedAttackGoal(this, new AcidAttack(this), 20),
                        //LookAtGoal(mob, targetType, lookDistance)
                        new LookAtPlayerGoal(this, Pig.class, 8.0F),
                        //LookRandomlyGoal(mob)
                        new RandomLookAroundGoal(this),
                        new OpenDoorGoal(this, true)
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
                        new TargetAttacker(this).setAlertAllies(),
                        new NearestLivingEntityTargetGoal<>(this, true, true)
                };
        return goals;
    }

    @Override
    protected int getExperienceReward(Player player)
    {
        return 3;
    }

    /** ~~~~~~~~ ANIMATION ~~~~~~~~ **/

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event)
    {
        //event.getController().setAnimation();
        if(event.isMoving())
        {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sculk_hatcher.walk", true));
        }
        else
        {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sculk_hatcher.idle", true));
        }

        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public TargetParameters getTargetParameters() {
        return TARGET_PARAMETERS;
    }

    /** ~~~~~~~~ CLASSES ~~~~~~~~ **/

    public class SculkHatcherAttackGoal extends MeleeAttackGoal
    {

        private final SculkHatcherEntity thisMob;
        private long tickCooldownForSpawn = 20 * 1;
        private long ticksInCooldown = 0;

        /**
         * The Constructor
         * @param mob The mob that called this
         * @param speedModifier How fast can they attack?
         * @param followTargetIfNotSeen Should the mob follow their target if they cant see them.
         */
        public SculkHatcherAttackGoal(SculkHatcherEntity mob, double speedModifier, boolean followTargetIfNotSeen) {
            super(mob, speedModifier, followTargetIfNotSeen);
            this.thisMob = mob;
        }

        @Override
        public boolean canUse()
        {
            boolean canWeUse = ((ISculkSmartEntity)this.mob).getTargetParameters().isEntityValidTarget(this.mob.getTarget(), true);
            // If the mob is already targeting something valid, don't bother
            return canWeUse;
        }

        @Override
        public boolean canContinueToUse()
        {
            return canUse();
        }


        /**
         * Gets called every tick the attack is active<br>
         * We shouldn't have to check if the target is null since
         * the super class does this. However, something funky is going on that
         * causes a null pointer exception if we dont check this here. This is
         * absolutely some sort of bug that I was unable to figure out. For the
         * time being (assuming I ever fix this), this will have to do.
         */
        public void tick()
        {
            if(this.thisMob.getTarget() == null)
            {
                stop();
            }
            else
            {
                super.tick();
                if(ticksInCooldown >= tickCooldownForSpawn && thisMob.getTarget() != null && thisMob.getHealth() > SculkMiteEntity.MAX_HEALTH)
                {
                    ticksInCooldown = 0;
                    BlockPos spawnPos = new BlockPos(thisMob.position());
                    EntityRegistry.SCULK_MITE.spawn((ServerLevel) thisMob.level, null, null, spawnPos, MobSpawnType.SPAWNER, true, true);
                    thisMob.hurt(DamageSource.GENERIC, SculkMiteEntity.MAX_HEALTH);
                }
                else
                {
                    ticksInCooldown++;
                }
            }
        }
    }
}
