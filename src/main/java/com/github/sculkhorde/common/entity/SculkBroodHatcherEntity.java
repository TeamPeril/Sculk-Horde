package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.components.*;
import com.github.sculkhorde.common.entity.goal.*;
import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.systems.squad_system.SquadSystem;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SculkBroodHatcherEntity extends Monster implements GeoEntity, ISculkSmartEntity {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited {@link com.github.sculkhorde.core.ModEntities}<br>
     * Edited {@link com.github.sculkhorde.util.ModEventSubscriber}<br>
     * Edited {@link com.github.sculkhorde.client.ClientModEventSubscriber}<br>
     * Added {@link com.github.sculkhorde.client.model.enitity.SculkBroodHatcherModel}<br>
     * Added {@link com.github.sculkhorde.client.renderer.entity.SculkBroodHatcherRenderer}
     */

    //The Health
    public static final float MAX_HEALTH = 20F;
    //The armor of the mob
    public static final float ARMOR = 6F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 4F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 1F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 32F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.35F;

    // Controls what types of entities this mob can target
    private TargetParameters TARGET_PARAMETERS = DefaultTargetParameters.DefaultGroundMeleeInfector.copy(this)
            .addRetentionRule(TargetRetention.maxDistance(FOLLOW_RANGE + 10));
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected SculkBroodlingEntity child1;
    protected SculkBroodlingEntity child2;
    protected SculkBroodlingEntity child3;

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkBroodHatcherEntity(EntityType<? extends SculkBroodHatcherEntity> type, Level worldIn) {
        super(type, worldIn);
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
    }
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

    @Override
    public void checkDespawn() {}

    private boolean isParticipatingInRaid = false;

    

    @Override
    public boolean isParticipatingInRaid() {
        return isParticipatingInRaid;
    }

    @Override
    public void setParticipatingInRaid(boolean isParticipatingInRaidIn) {
        isParticipatingInRaid = isParticipatingInRaidIn;
    }

    @Override
    public TargetParameters getTargetParameters() {
        return TARGET_PARAMETERS;
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
                        new DespawnAfterTime(this, TickUnits.convertMinutesToTicks(15)),
                        new DespawnWhenIdle(this, TickUnits.convertMinutesToTicks(10)),
                        //SwimGoal(mob)
                        new FloatGoal(this),
                        new SquadLogicGoal(this),
                        new LeapAtTargetGoal(this, 0.7F),
                        new AttackGoal(),
                        new FollowSquadLeader(this),
                        new PathFindToRaidLocation<>(this),
                        //WaterAvoidingRandomWalkingGoal(mob, speedModifier)
                        new ImprovedRandomStrollGoal(this, 0.5D).setToAvoidWater(true),
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
                        new TargetAttacker(this),
                        new FocusSquadTarget(this),
                        new SculkHordeTargetGoal<>(this)
                };
        return goals;
    }

    @Override
    protected @NotNull PathNavigation createNavigation(Level p_33802_) {
        return new WallClimberNavigation(this, p_33802_);
    }
    @Override
    public void makeStuckInBlock(BlockState p_33796_, Vec3 p_33797_) {
        if (p_33796_.is(Blocks.COBWEB)) {
            // do nothing
        }

    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        TARGET_PARAMETERS.updateTargets();

        // I know this code is simple and kinda dumb, but idc. It works just fine
        if(child1 == null || child1.isDeadOrDying())
        {
            child1 = new SculkBroodlingEntity(level(), blockPosition());
            level().addFreshEntity(child1);

            if(SquadSystem.getSquadOfLivingEntity(this).isPresent())
            {
                SquadSystem.getSquadOfLivingEntity(this).get().forceAcceptMemberIntoSquad(child1);
            }
        }

        if(child2 == null || child2.isDeadOrDying())
        {
            child2 = new SculkBroodlingEntity(level(), blockPosition());
            level().addFreshEntity(child2);

            if(SquadSystem.getSquadOfLivingEntity(this).isPresent())
            {
                SquadSystem.getSquadOfLivingEntity(this).get().forceAcceptMemberIntoSquad(child2);
            }
        }

        if(child3 == null || child3.isDeadOrDying())
        {
            child3 = new SculkBroodlingEntity(level(), blockPosition());
            level().addFreshEntity(child3);

            if(SquadSystem.getSquadOfLivingEntity(this).isPresent())
            {
                SquadSystem.getSquadOfLivingEntity(this).get().forceAcceptMemberIntoSquad(child3);
            }
        }
    }

    private static final RawAnimation ATTACK_ANIMATION = RawAnimation.begin().thenPlay("attack");

    private final AnimationController ATTACK_ANIMATION_CONTROLLER = new AnimationController<>(this, "attack_controller", state -> PlayState.STOP)
            .triggerableAnim("attack", ATTACK_ANIMATION).transitionLength(5);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                DefaultAnimations.genericWalkRunIdleController(this),
                ATTACK_ANIMATION_CONTROLLER,
                DefaultAnimations.genericLivingController(this)
        );
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.SPIDER_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.SPIDER_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.0F);
    }

    public boolean dampensVibrations() {
        return true;
    }


    /* DO NOT USE THIS FOR ANYTHING, CAUSES DESYNC
    @Override
    public void onRemovedFromWorld() {
        ModSavedData.getSaveData().addSculkAccumulatedMass((int) this.getHealth());
        super.onRemovedFromWorld();
    }
    */

    class AttackGoal extends CustomMeleeAttackGoal
    {

        public AttackGoal()
        {
            super(SculkBroodHatcherEntity.this, 1.0D, true, 10);
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

        protected double getAttackReachBlocks()
        {
            return 3.5F;
        }

        @Override
        protected int getAttackInterval() {
            return TickUnits.convertSecondsToTicks(0.5F);
        }

        @Override
        protected void triggerAnimation() {
            //((SculkBroodHatcherEntity)mob).triggerAnim("attack_controller", "attack");
        }

        @Override
        public void onTargetHurt(LivingEntity target) {
            super.onTargetHurt(target);
            if(target == null)
            {
                return;
            }

            target.addEffect(new MobEffectInstance(ModMobEffects.ROOTED_EFFECT.get(), TickUnits.convertMinutesToTicks(2), 0), this.mob);
        }
    }
}
