package com.github.sculkhoard.common.entity;

import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Random;

public class SculkZombieEntity extends MonsterEntity implements IAnimatable {

    /* NOTE: In order to create a mob, there is a lot of things that need to be created/modified
     * For this entity, I created/modified the following files:
     * Edited core/ EntityRegistry.java
     * Edited util/ ModEventSubscriber.java
     * Edited client/ ClientModEventSubscriber.java
     * Added common/entity/ SculkZombieEntity.java
     * Added client/model/entity/ SculkZombieModel.java
     * Added client/renderer/entity/ SculkZombieRenderer.java
     */

    private AnimationFactory factory = new AnimationFactory(this);

    //Constructor
    public SculkZombieEntity(EntityType<? extends MonsterEntity> type, World worldIn) {
        super(type, worldIn);
    }

    /* passSpawnCondition
     * @description A function that is called in ModEventSubscriber.java to give
     * this mob its attributes.
     */
    public static AttributeModifierMap.MutableAttribute createAttributes()
    {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.FOLLOW_RANGE,50.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.20D);
    }

    /* passSpawnCondition
     * @description determines whether a given possible spawn location meets your criteria
     * @param config ???
     * @param world The dimension the mob is attempting to be spawned in??
     * @param reason Specifies on why a mob is attempting to be spawned.
     * @param pos The Block Coordinates that the mob is being attempted to spawn at.
     * @param random ???
     */
    public static boolean passSpawnCondition(EntityType<? extends CreatureEntity> config, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
        // peaceful check
        if (world.getDifficulty() == Difficulty.PEACEFUL)
            return false;
        // pass through if natural spawn and using individual spawn rules
        if ((reason != SpawnReason.CHUNK_GENERATION && reason != SpawnReason.NATURAL))
            return false;
        return true;
    }

    @Override
    public void registerGoals() {
        super.registerGoals();

        //NearestAttackableTargetGoal(Mob, targetType, mustSee)
        this.goalSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));

        //NearestAttackableTargetGoal(Mob, targetType, mustSee)
        //this.goalSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true));

        //MeleeAttackGoal(mob, speedModifier, followingTargetEvenIfNotSeen)
        //this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));

        //MoveTowardsTargetGoal(mob, speedModifier, within)
        this.goalSelector.addGoal(4, new MoveTowardsTargetGoal(this, 0.8F, 20F));

        //WaterAvoidingRandomWalkingGoal(mob, speedModifier)
        //this.goalSelector.addGoal(7, new WaterAvoidingRandomWalkingGoal(this, 0.75F));

        //LookAtGoal(mob, targetType, lookDistance)
        this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 8.0F));

        //LookRandomlyGoal(mob)
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));

        //NearestAttackableTargetGoal(Mob, targetType, mustSee)
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));

        //NearestAttackableTargetGoal(Mob, targetType, mustSee)
        //this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true));
    }

    @Override
    protected int getExperienceReward(PlayerEntity player)
    {
        return 3;
    }

    @Override
    public boolean doHurtTarget(Entity entityIn)
    {
        boolean flag = super.doHurtTarget(entityIn);
        if(!flag)
        {
            return false;
        }
        else
        {
            if(entityIn instanceof LivingEntity)
            {
                ((LivingEntity) entityIn).addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 280, 1));
            }
            return true;
        }
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
