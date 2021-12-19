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
     * Edited EntityRegistry.java
     * Edited ModEventSubscriber.java
     * Edited ClientModEventSubscriber.java
     * Added SculkZombieEntity.java
     * Added SculkZombieModel.java
     * Added SculkZombieRenderer.java
     */

    private AnimationFactory factory = new AnimationFactory(this);

    public SculkZombieEntity(EntityType<? extends MonsterEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public static AttributeModifierMap.MutableAttribute createAttributes()
    {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 18.0D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.FOLLOW_RANGE,50)
                .add(Attributes.MOVEMENT_SPEED, 0.01D);
    }

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
        this.goalSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true));

        //MeleeAttackGoal(mob, speedModifier, followingTargetEvenIfNotSeen)
        //this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));

        //MoveTowardsTargetGoal(mob, speedModifier, within)
        this.goalSelector.addGoal(4, new MoveTowardsTargetGoal(this, 1.0, 20F));

        //WaterAvoidingRandomWalkingGoal(mob, speedModifier)
        this.goalSelector.addGoal(7, new WaterAvoidingRandomWalkingGoal(this, 0));

        //LookAtGoal(mob, targetType, lookDistance)
        this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 8.0F));

        //LookRandomlyGoal(mob)
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));

        //NearestAttackableTargetGoal(Mob, targetType, mustSee)
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));

        //NearestAttackableTargetGoal(Mob, targetType, mustSee)
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true));
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
