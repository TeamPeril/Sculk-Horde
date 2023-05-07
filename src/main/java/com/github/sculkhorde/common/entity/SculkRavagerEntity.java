package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.client.model.enitity.SculkRavagerModel;
import com.github.sculkhorde.client.renderer.entity.SculkRavagerRenderer;
import com.github.sculkhorde.common.entity.goal.*;
import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.TargetParameters;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * In order to create a mob, the following java files were created/edited.<br>
 * Edited {@link EntityRegistry}<br>
 * Edited {@link com.github.sculkhorde.util.ModEventSubscriber}<br>
 * Edited {@link com.github.sculkhorde.client.ClientModEventSubscriber}<br>
 * Edited {@link com.github.sculkhorde.common.world.ModWorldEvents} (this might not be necessary)<br>
 * Edited {@link com.github.sculkhorde.common.world.gen.ModEntityGen}<br>
 * Added {@link SculkRavagerEntity}<br>
 * Added {@link SculkRavagerModel} <br>
 * Added {@link SculkRavagerRenderer}
 */
public class SculkRavagerEntity extends Ravager implements GeoEntity, ISculkSmartEntity {


    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkRavagerEntity(EntityType<? extends SculkRavagerEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public SculkRavagerEntity(Level worldIn) {super(EntityRegistry.SCULK_RAVAGER.get(), worldIn);}

    //The Health
    public static final float MAX_HEALTH = 50F;
    //The armor of the mob
    public static final float ARMOR = 4F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 18F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 5F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 50F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.75F;

    // Controls what types of entities this mob can target
    private TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetHostiles().enableTargetInfected().enableMustReachTarget();

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /**
     * Determines & registers the attributes of the mob.
     * @return The Attributes
     */
    public static AttributeSupplier.Builder createAttributes()
    {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 50F)
                .add(Attributes.ARMOR, ARMOR)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.ATTACK_KNOCKBACK, ATTACK_KNOCKBACK)
                .add(Attributes.FOLLOW_RANGE,FOLLOW_RANGE)
                .add(Attributes.MOVEMENT_SPEED, 0.4F)
                .add(Attributes.KNOCKBACK_RESISTANCE, MOVEMENT_SPEED);
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

    public boolean isIdle() {
        return getTarget() == null;
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
        return new Goal[]{

                new DespawnWhenIdle(this, 120),
                //SwimGoal(mob)
                new FloatGoal(this),
                //MeleeAttackGoal(mob, speedModifier, followingTargetEvenIfNotSeen)
                new AttackGoal(),
                new PathFindToRaidLocation<>(this),
                //WaterAvoidingRandomWalkingGoal(mob, speedModifier)
                new WaterAvoidingRandomStrollGoal(this, 0.7D),
                // new LookAtGoal(this, LivingEntity.class, 6.0F),
                // new LookAtGoal(this, MobEntity.class, 8.0F)
        };

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
        return new Goal[]{
                new InvalidateTargetGoal(this),
                new TargetAttacker(this).setAlertAllies(),
                new NearestLivingEntityTargetGoal<>(this, true, true)
        };
    }


    /**
     * If a sculk living entity despawns, refund it's current health to the sculk hoard
     */
    @Override
    public void onRemovedFromWorld() {
        SculkHorde.savedData.addSculkAccumulatedMass((int) this.getHealth());
        super.onRemovedFromWorld();
    }

    /** ~~~~~~~~ ANIMATION ~~~~~~~~ **/

    private static final RawAnimation HEAD_ATTACK_ANIMATION = RawAnimation.begin().thenPlay("head.attack");

    // Add our animations
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericWalkIdleController(this));
        controllers.add(DefaultAnimations.genericAttackAnimation(this, HEAD_ATTACK_ANIMATION));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /** ~~~~~~~~ CLASSES ~~~~~~~~ **/

    class AttackGoal extends MeleeAttackGoal
    {
        public AttackGoal()
        {
            super(SculkRavagerEntity.this, 1.0D, true);
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

        protected double getAttackReachSqr(LivingEntity pAttackTarget)
        {
            float f = SculkRavagerEntity.this.getBbWidth() - 0.1F;
            return (double)(f * 2.0F * f * 2.0F + pAttackTarget.getBbWidth());
        }
    }

    public boolean dampensVibrations() {
        return true;
    }
}
