package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.client.model.enitity.SculkRavagerModel;
import com.github.sculkhorde.client.renderer.entity.SculkRavagerRenderer;
import com.github.sculkhorde.common.entity.goal.NearestLivingEntityTargetGoal;
import com.github.sculkhorde.common.entity.goal.TargetAttacker;
import com.github.sculkhorde.core.EntityRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.monster.RavagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

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
public class SculkRavagerEntity extends RavagerEntity implements IAnimatable {


    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkRavagerEntity(EntityType<? extends SculkRavagerEntity> type, World worldIn) {
        super(type, worldIn);
    }

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public SculkRavagerEntity(World worldIn) {super(EntityRegistry.SCULK_RAVAGER, worldIn);}

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

    /**
     * Determines & registers the attributes of the mob.
     * @return The Attributes
     */
    public static AttributeModifierMap.MutableAttribute createAttributes()
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
            this.goalSelector.addGoal(priority, targetSelectorPayload[priority]);
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
                //SwimGoal(mob)
                new SwimGoal(this),
                //MeleeAttackGoal(mob, speedModifier, followingTargetEvenIfNotSeen)
                new AttackGoal(),
                //WaterAvoidingRandomWalkingGoal(mob, speedModifier)
                new WaterAvoidingRandomWalkingGoal(this, 0.4D),
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
                new TargetAttacker(this).setAlertSculkLivingEntities(),
                new NearestLivingEntityTargetGoal<>(this, true, true)
                        .enableDespawnWhenIdle().enableTargetHostiles().enableTargetPassives().ignoreTargetBelow50PercentHealth()
        };
    }

    @Override
    protected int getExperienceReward(PlayerEntity player)
    {
        return 3;
    }

    /** ~~~~~~~~ ANIMATION ~~~~~~~~ **/

    private <E extends IAnimatable> PlayState tumorPredicate(AnimationEvent<E> event)
    {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.ravager.tumors_idle", true));
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState mouthPredicate(AnimationEvent<E> event)
    {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.ravager.mouth_idle", true));
        return PlayState.CONTINUE;
    }

    AnimationController tumorController = new AnimationController(this, "tumor_controller", 0, this::tumorPredicate);
    AnimationController mouthController = new AnimationController(this, "mouth_controller", 0, this::mouthPredicate);
    AnimationFactory factory = new AnimationFactory(this);
    @Override
    public void registerControllers(AnimationData data)
    {
        data.addAnimationController(tumorController);
        data.addAnimationController(mouthController);
    }

    @Override
    public AnimationFactory getFactory()
    {
        return factory;
    }

    /** ~~~~~~~~ CLASSES ~~~~~~~~ **/

    class AttackGoal extends MeleeAttackGoal
    {
        public AttackGoal()
        {
            super(SculkRavagerEntity.this, 1.0D, true);
        }

        protected double getAttackReachSqr(LivingEntity pAttackTarget)
        {
            float f = SculkRavagerEntity.this.getBbWidth() - 0.1F;
            return (double)(f * 2.0F * f * 2.0F + pAttackTarget.getBbWidth());
        }
    }
}
