package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.goal.TargetAttacker;
import com.github.sculkhorde.core.EntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.function.Predicate;

public class SculkBeeInfectorEntity extends SculkBeeHarvesterEntity implements IAnimatable {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited core/ EntityRegistry.java<br>
     * Edited util/ ModEventSubscriber.java<br>
     * Edited client/ ClientModEventSubscriber.java<br>
     * Added common/entity/ SculkBeeInfectorEntity.java<br>
     * Added client/model/entity/ SculkBeeInfectorModel.java<br>
     * Added client/renderer/entity/ SculkBeeInfectorRenderer.java
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
    //MOVEMENT_SPEED determines how fast this mob moves
    public static final float MOVEMENT_SPEED = 0.5F;

    private AnimationFactory factory = new AnimationFactory(this);

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkBeeInfectorEntity(EntityType<? extends SculkBeeInfectorEntity> type, World worldIn) {
        super(type, worldIn);
    }

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public SculkBeeInfectorEntity(World worldIn) {super(EntityRegistry.SCULK_BEE_INFECTOR, worldIn);}

    /**
     * Determines & registers the attributes of the mob.
     * @return The Attributes
     */
    public static AttributeModifierMap.MutableAttribute createAttributes()
    {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.FOLLOW_RANGE,FOLLOW_RANGE)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.FLYING_SPEED, 1.5F);
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
        beePollinateGoal = new InfectFlowersGoal();
        goToHiveGoal = new FindBeehiveGoal();
        goToKnownFlowerGoal = new FindFlowerGoal();

        Goal[] goals =
                {
                        new UpdateBeehiveGoal(),
                        new EnterBeehiveGoal(),
                        beePollinateGoal,
                        goToHiveGoal,
                        goToKnownFlowerGoal,


                        //LookRandomlyGoal(mob)
                        new LookRandomlyGoal(this),
                        new SculkBeeHarvesterEntity.WanderGoal(),
                        new SwimGoal(this),
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
                        new TargetAttacker(this).setAlertSculkLivingEntities(),
                };
        return goals;
    }

    /** ~~~~~~~~ ANIMATION ~~~~~~~~ **/

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event)
    {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sculk_bee.flying", true));
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


    /**
     * Represents a predicate (boolean-valued function) of one argument. <br>
     * Currently determines if a block is a valid flower.
     */
    private final Predicate<BlockState> VALID_INFECTABLE_BLOCKS = (validBlocksPredicate) ->
    {
        if (validBlocksPredicate.is(BlockTags.TALL_FLOWERS))
        {
            if (validBlocksPredicate.is(Blocks.SUNFLOWER))
            {
                return validBlocksPredicate.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return validBlocksPredicate.is(BlockTags.SMALL_FLOWERS) || validBlocksPredicate.is(Blocks.GRASS);
        }
    };

    /**
     * Determines what flowers the bee can infect
     * @return The predicate
     */
    @Override
    protected Predicate<BlockState> getFlowerPredicate()
    {
        return VALID_INFECTABLE_BLOCKS;
    }

    /** CLASSES **/

    private class InfectFlowersGoal extends SculkBeeHarvesterEntity.PollinateGoal
    {
        /**
         * Constructor
         */
        InfectFlowersGoal()
        {
            super();
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        @Override
        public void stop()
        {
            //If entity has pollinated for enough time
            if (this.hasPollinatedLongEnough())
            {
                //Set entity to have nectar
                SculkBeeInfectorEntity.this.setHasNectar(true);

                // Spawn Block Traverser under bee
                BlockPos spreadPos = SculkBeeInfectorEntity.this.blockPosition().below();
                ServerWorld world = (ServerWorld) SculkBeeInfectorEntity.this.level;

                // Spawn Block Traverser
                BlockTraverserEntity blockTraverserEntity = new BlockTraverserEntity(EntityRegistry.BLOCK_TRAVERSER, world);
                blockTraverserEntity.setPos(spreadPos.getX(), spreadPos.getY(), spreadPos.getZ());
                world.addFreshEntity(blockTraverserEntity);

            }

            //Set pollination to false
            this.pollinating = false;
            //Stop navigation
            SculkBeeInfectorEntity.this.navigation.stop();
            //reset cooldown
            SculkBeeInfectorEntity.this.remainingCooldownBeforeLocatingNewFlower = 200;
        }

    } //END POLLINATE GOAL

}
