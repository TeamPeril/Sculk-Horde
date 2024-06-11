package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.goal.*;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.SquadHandler;
import com.github.sculkhorde.util.TargetParameters;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class SculkWitchEntity extends Monster implements GeoEntity, ISculkSmartEntity, RangedAttackMob {

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

    //The Health
    public static final float MAX_HEALTH = 10F;
    //The armor of the mob
    public static final float ARMOR = 10F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 7F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 1F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 25F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.30F;

    // Controls what types of entities this mob can target
    private TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetHostiles().enableTargetInfected().enableMustReachTarget();
    private SquadHandler squad = new SquadHandler(this);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkWitchEntity(EntityType<? extends SculkWitchEntity> type, Level worldIn) {
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

    @Override
    public void checkDespawn() {}

    public boolean isIdle() {
        return getTarget() == null;
    }

    private boolean isParticipatingInRaid = false;

    @Override
    public SquadHandler getSquad() {
        return squad;
    }

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
                        new DespawnAfterTime(this, TickUnits.convertMinutesToTicks(5)),
                        new DespawnWhenIdle(this, TickUnits.convertMinutesToTicks(2)),
                        new OpenDoorGoal(this, true),
                        //SwimGoal(mob)
                        new FloatGoal(this),
                        new SquadHandlingGoal(this),
                        new ChaseFriendlyAndHeal(this),
                        new RangedAttackGoal(this, 1.0D, 60, 10.0F),
                        new FollowSquadLeader(this),
                        new PathFindToRaidLocation<>(this),
                        //MoveTowardsTargetGoal(mob, speedModifier, within) THIS IS FOR NON-ATTACKING GOALS
                        new MoveTowardsTargetGoal(this, 0.8F, 20F),
                        //WaterAvoidingRandomWalkingGoal(mob, speedModifier)
                        new ImprovedRandomStrollGoal(this, 1.0D).setToAvoidWater(true),

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
                        new TargetAttacker(this),
                        new NearestLivingEntityTargetGoal<>(this, true, true)

                };
        return goals;
    }

    public void performRangedAttack(LivingEntity target, float p_34144_) {
        Vec3 vec3 = target.getDeltaMovement();
        double d0 = target.getX() + vec3.x - this.getX();
        double d1 = target.getEyeY() - (double)1.1F - this.getY();
        double d2 = target.getZ() + vec3.z - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        Potion potion = Potions.HARMING;
        if (EntityAlgorithms.isSculkLivingEntity.test(target)) {
            if (target.getHealth() <= 4.0F)
            {
                potion = Potions.HEALING;
            }
            else if(random.nextFloat() < 0.50F)
            {
                potion = Potions.REGENERATION;
            }
            else
            {
                potion = Potions.STRENGTH;
            }

            this.setTarget((LivingEntity)null);
        } else if (d3 >= 8.0D && !target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            potion = Potions.SLOWNESS;
        } else if (target.getHealth() >= 8.0F && !target.hasEffect(MobEffects.POISON)) {
            potion = Potions.POISON;
        } else if (d3 <= 3.0D && !target.hasEffect(MobEffects.WEAKNESS) && this.random.nextFloat() < 0.25F) {
            potion = Potions.WEAKNESS;
        }

        ThrownPotion thrownpotion = new ThrownPotion(this.level(), this);
        thrownpotion.setItem(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), potion));
        thrownpotion.setXRot(thrownpotion.getXRot() - -20.0F);
        thrownpotion.shoot(d0, d1 + d3 * 0.2D, d2, 0.75F, 8.0F);
        if (!this.isSilent()) {
            this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_THROW, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
        }

        this.level().addFreshEntity(thrownpotion);

    }

    public void performHealAttack(LivingEntity target) {
        Vec3 vec3 = target.getDeltaMovement();
        double d0 = target.getX() + vec3.x - this.getX();
        double d1 = target.getEyeY() - (double)1.1F - this.getY();
        double d2 = target.getZ() + vec3.z - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        Potion potion = Potions.HARMING;
        if (EntityAlgorithms.isSculkLivingEntity.test(target)) {
            if (target.getHealth() <= 4.0F)
            {
                potion = Potions.HEALING;
            }
            else if(random.nextFloat() < 0.50F)
            {
                potion = Potions.REGENERATION;
            }
            else
            {
                potion = Potions.STRENGTH;
            }

            this.setTarget((LivingEntity)null);
        }

        ThrownPotion thrownpotion = new ThrownPotion(this.level(), this);
        thrownpotion.setItem(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), potion));
        thrownpotion.setXRot(thrownpotion.getXRot() - -20.0F);
        thrownpotion.shoot(d0, d1 + d3 * 0.2D, d2, 0.75F, 8.0F);
        if (!this.isSilent()) {
            this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_THROW, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
        }

        this.level().addFreshEntity(thrownpotion);

    }

    // Animation Code

    private static final RawAnimation JAW_IDLE_ANIMATION = RawAnimation.begin().thenLoop("jaw.idle");
    private static final RawAnimation JAW_RUN_ANIMATION = RawAnimation.begin().thenLoop("jaw.run");
    private static final RawAnimation TUMOR_ANIMATION = RawAnimation.begin().thenLoop("tumor");
    private static final RawAnimation ATTACK_ANIMATION = RawAnimation.begin().thenPlay("jaw.attack");

    private final AnimationController ATTACK_ANIMATION_CONTROLLER = new AnimationController<>(this, "attack_controller", state -> PlayState.STOP)
            .triggerableAnim("attack_animation", ATTACK_ANIMATION).transitionLength(5);



    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                //DefaultAnimations.genericWalkRunIdleController(this).transitionLength(5),
                //ATTACK_ANIMATION_CONTROLLER,
                //new AnimationController<>(this, "Legs", 5, this::poseJawCycle),
                //new AnimationController<>(this, "Tumor", 5, this::poseTumorCycle)
        );
    }

    // Create the animation handler for the body segment
    protected PlayState poseJawCycle(AnimationState<SculkWitchEntity> state)
    {
        if(!state.isMoving())
        {
            state.setAnimation(JAW_IDLE_ANIMATION);
        }
        else
        {
            state.setAnimation(JAW_RUN_ANIMATION);
        }

        return PlayState.CONTINUE;
    }

    protected PlayState poseTumorCycle(AnimationState<SculkWitchEntity> state)
    {
        state.setAnimation(TUMOR_ANIMATION);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.PILLAGER_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.PILLAGER_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.PILLAGER_DEATH;
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(SoundEvents.DROWNED_STEP, 0.15F, 1.0F);
    }

    public boolean dampensVibrations() {
        return true;
    }


    private class ChaseFriendlyAndHeal extends Goal {

        private final ISculkSmartEntity thisEntity; // the skeleton mob
        private int timeToRecalcPath;
        long lastTimeOfCheck = 0;
        long CHECK_INTERVAL = TickUnits.convertSecondsToTicks(5);
        private final int REQUIRED_PROXIMITY = 5;
        LivingEntity targetToHeal = null;

        private long lastTimeOfPotionThrown = 0;
        private final long POTION_THROW_COOLDOWN = TickUnits.convertSecondsToTicks(5);

        public ChaseFriendlyAndHeal(ISculkSmartEntity mob) {
            this.thisEntity = mob;
        }

        private Mob getMob() {
            return (Mob) this.thisEntity;
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse()
        {
            long currentTime = getMob().level().getGameTime();
            long timeElapsed = currentTime - lastTimeOfCheck;
            boolean hasEnoughTimeElapsed = timeElapsed >= CHECK_INTERVAL;

            List<LivingEntity> hurtSculkUnits = new ArrayList<>();

            if(hasEnoughTimeElapsed)
            {
                hurtSculkUnits = EntityAlgorithms.getHurtSculkHordeEntitiesInBoundingBox((ServerLevel) getMob().level(), EntityAlgorithms.createBoundingBoxCubeAtBlockPos(position(), 15));
                if(!hurtSculkUnits.isEmpty()) { targetToHeal = hurtSculkUnits.get(0); }

                lastTimeOfCheck = level().getGameTime();
            }

            return targetToHeal != null;
        }

        @Override
        public boolean canContinueToUse() {
            return verifyHealTarget();
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        @Override
        public void start()
        {
            this.timeToRecalcPath = 0;
            lastTimeOfCheck = level().getGameTime();
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public void tick()
        {
            if(level().isClientSide())
            {
                return;
            }

            if(!verifyHealTarget())
            {
                return;
            }

            if (getMob().distanceToSqr(targetToHeal) < REQUIRED_PROXIMITY && level().getGameTime() - lastTimeOfPotionThrown > POTION_THROW_COOLDOWN) {
                // stop the navigation
                performHealAttack(targetToHeal);
                lastTimeOfPotionThrown = level().getGameTime();
            }

            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(20);
                this.getMob().getNavigation().moveTo(targetToHeal, 1.0);
            }
        }

        protected boolean verifyHealTarget()
        {
            if (targetToHeal == null)
            {
                return false;
            }

            if(targetToHeal.getHealth() >= targetToHeal.getMaxHealth())
            {
                targetToHeal = null;
                return false;
            }
            if(targetToHeal.getHealth() <= 0)
            {
                targetToHeal = null;
                return false;
            }

            return true;
        }
    }



}
