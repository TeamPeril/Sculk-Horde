package com.github.sculkhoard.common.entity;

import com.github.sculkhoard.common.entity.goal.SculkZombieAttackGoal;
import com.github.sculkhoard.core.BlockRegistry;
import com.github.sculkhoard.core.EntityRegistry;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.extensions.IForgeEntity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Random;

public class SculkZombieEntity extends SculkLivingEntity implements IAnimatable {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited core/ EntityRegistry.java<br>
     * Edited util/ ModEventSubscriber.java<br>
     * Edited client/ ClientModEventSubscriber.java<br>
     * Edited common/world/ModWorldEvents.java (this might not be necessary)<br>
     * Edited common/world/gen/ModEntityGen.java<br>
     * Added common/entity/ SculkZombie.java<br>
     * Added client/model/entity/ SculkZombieModel.java<br>
     * Added client/renderer/entity/ SculkZombieRenderer.java
     */

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
     * factory The animation factory used for animations
     */
    public static int SPAWN_MIN = 1;
    public static int SPAWN_MAX = 3;
    public static int SPAWN_Y_MAX = 15;
    private AnimationFactory factory = new AnimationFactory(this);

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkZombieEntity(EntityType<? extends SculkZombieEntity> type, World worldIn) {
        super(type, worldIn);
    }

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public SculkZombieEntity(World worldIn) {super(EntityRegistry.SCULK_ZOMBIE.get(), worldIn);}

    /**
     * Determines & registers the attributes of the mob.
     * @return The Attributes
     */
    public static AttributeModifierMap.MutableAttribute createAttributes()
    {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D)
                .add(Attributes.FOLLOW_RANGE,25.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D);
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
        Goal[] goals =
                {
                        //SwimGoal(mob)
                        new SwimGoal(this),
                        //MeleeAttackGoal(mob, speedModifier, followingTargetEvenIfNotSeen)
                        new SculkZombieAttackGoal(this, 1.0D, true),
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
                        //HurtByTargetGoal(mob)
                        new HurtByTargetGoal(this).setAlertOthers(), //This doesnt work
                        //NearestAttackableTargetGoal(Mob, targetType, mustSee)
                        new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true),
                        //NearestAttackableTargetGoal(Mob, targetType, mustSee)
                        new NearestAttackableTargetGoal<>(this, IronGolemEntity.class, true),

                };
        return goals;
    }




    @Override
    protected int getExperienceReward(PlayerEntity player)
    {
        return 3;
    }

    //Animation Related Functions

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
}
