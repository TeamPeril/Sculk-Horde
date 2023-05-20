package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.goal.*;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.TargetParameters;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;

public class SculkEndermanEntity extends Monster implements GeoEntity, ISculkSmartEntity {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited {@link com.github.sculkhorde.core.EntityRegistry}<br>
     * Edited {@link com.github.sculkhorde.util.ModEventSubscriber}<br>
     * Edited {@link com.github.sculkhorde.client.ClientModEventSubscriber}<br>
     * Added {@link SculkEndermanEntity}<br>
     * Added {@link com.github.sculkhorde.client.model.enitity.SculkEndermanModel}<br>
     * Added {@link com.github.sculkhorde.client.renderer.entity.SculkEndermanRenderer}
     */

    //The Health
    public static final float MAX_HEALTH = 40F;
    //The armor of the mob
    public static final float ARMOR = 4F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 7F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 1F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 64F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.3F;

    // Controls what types of entities this mob can target
    private TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetHostiles().enableTargetInfected().disableBlackListMobs();

    // Timing Variables
    protected int TELEPORT_COOLDOWN = TickUnits.convertSecondsToTicks(8);
    protected int ticksSinceLastTeleport = 0;

    protected int SPECIAL_ATTACK_COOLDOWN = TickUnits.convertSecondsToTicks(5);
    protected int ticksSinceLastSpecialAttack = 0;

    // Data
    private static final EntityDataAccessor<Boolean> DATA_ANGRY = SynchedEntityData.defineId(SculkEndermanEntity.class, EntityDataSerializers.BOOLEAN);

    // Animation
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkEndermanEntity(EntityType<? extends SculkEndermanEntity> type, Level worldIn) {
        super(type, worldIn);
        this.setMaxUpStep(1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
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

    // Accessors and Modifiers

    public boolean isIdle() {
        return getTarget() == null;
    }

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

    boolean isLookingAtMe(Player player)
    {
        Vec3 vec3 = player.getViewVector(1.0F).normalize();
        Vec3 vec31 = new Vec3(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ());
        double d0 = vec31.length();
        vec31 = vec31.normalize();
        double d1 = vec3.dot(vec31);
        return d1 > 1.0D - 0.025D / d0 ? player.hasLineOfSight(this) : false;

    }

    @Override
    public void checkDespawn() {}

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
                        new FloatGoal(this),
                        //MeleeAttackGoal(mob, speedModifier, followingTargetEvenIfNotSeen)
                        new MeleeAttackGoal(this, 1.0D, true),
                        new PathFindToRaidLocation<>(this),
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
                        //HurtByTargetGoal(mob)
                        new TargetAttacker(this).setAlertAllies(),
                        new NearestLivingEntityTargetGoal<>(this, true, true)

                };
        return goals;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void aiStep()
    {
        if (this.level.isClientSide) {
            for(int i = 0; i < 2; ++i) {
                this.level.addParticle(ParticleTypes.PORTAL, this.getRandomX(0.5D), this.getRandomY() - 0.25D, this.getRandomZ(0.5D), (this.random.nextDouble() - 0.5D) * 2.0D, -this.random.nextDouble(), (this.random.nextDouble() - 0.5D) * 2.0D);
            }
        }

        this.jumping = false;
        super.aiStep();
    }

    /**
     * Called every tick to update the entity's position/logic.
     */
    protected void customServerAiStep()
    {
        ticksSinceLastTeleport++;
        if ((getTarget() == null && ticksSinceLastTeleport >= TELEPORT_COOLDOWN) || (getTarget() != null && ticksSinceLastTeleport >= TELEPORT_COOLDOWN/8))
        {
            ticksSinceLastTeleport = 0;
            if(getTarget() == null)
            {
                this.teleport();
            }
            else
            {
                teleportTowards(getTarget());
            }
        }
        super.customServerAiStep();
    }

    /**
     * Teleports the entity to a random position within 64 blocks of the entity
     * @return Returns true if the teleport was successful
     */
    protected boolean teleport()
    {
        if (!this.level.isClientSide() && this.isAlive())
        {
            double teleportDistance = 64.0D;
            double d0 = this.getX() + (this.random.nextDouble() - 0.5D) * teleportDistance;
            double d1 = this.getY() + (double)(this.random.nextInt(64) - 32);
            double d2 = this.getZ() + (this.random.nextDouble() - 0.5D) * teleportDistance;
            return this.teleport(d0, d1, d2);
        } else {
            return false;
        }
    }

    /**
     * Teleports the entity towards the given entity
     * @param entity The entity to teleport towards
     * @return Returns true if the teleport was successful
     */
    boolean teleportTowards(Entity entity) {
        Vec3 vec3 = new Vec3(this.getX() - entity.getX(), this.getY(0.5D) - entity.getEyeY(), this.getZ() - entity.getZ());
        vec3 = vec3.normalize();
        double teleportDistance = 8.0D;
        double d1 = this.getX() + (this.random.nextDouble() - 0.5D) * 8.0D - vec3.x * teleportDistance;
        double d2 = this.getY() + (double)(this.random.nextInt(16) - 8) - vec3.y * teleportDistance;
        double d3 = this.getZ() + (this.random.nextDouble() - 0.5D) * 8.0D - vec3.z * teleportDistance;
        return this.teleport(d1, d2, d3);
    }

    /**
     * Teleport the enderman to a random nearby position
     * @param x The x position
     * @param y The y position
     * @param z The z position
     * @return Returns true if the teleport was successful
     */
    private boolean teleport(double x, double y, double z)
    {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(x, y, z);

        while(blockpos$mutableblockpos.getY() > this.level.getMinBuildHeight() && !this.level.getBlockState(blockpos$mutableblockpos).getMaterial().blocksMotion())
        {
            blockpos$mutableblockpos.move(Direction.DOWN);
        }

        BlockState blockstate = this.level.getBlockState(blockpos$mutableblockpos);
        boolean isMotionBlockFlag = blockstate.getMaterial().blocksMotion();
        boolean isWaterFlag = blockstate.getFluidState().is(FluidTags.WATER);
        if (isMotionBlockFlag && !isWaterFlag)
        {
            net.minecraftforge.event.entity.EntityTeleportEvent.EnderEntity event = net.minecraftforge.event.ForgeEventFactory.onEnderTeleport(this, x, y, z);
            if (event.isCanceled())
            {
                return false;
            }
            Vec3 vec3 = this.position();
            boolean ifCanRandomTeleport = this.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true);
            if (ifCanRandomTeleport)
            {
                this.level.gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(this));
                if (!this.isSilent())
                {
                    this.level.playSound((Player)null, this.xo, this.yo, this.zo, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
                    this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
            }

            return ifCanRandomTeleport;
        }
        else
        {
            return false;
        }
    }

    // ###### Data Code ########
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(DATA_ANGRY, false);
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> p_32513_) {
        super.onSyncedDataUpdated(p_32513_);
    }

    public boolean isSensitiveToWater() {
        return true;
    }

    // ####### Animation Code ###########

    private final AnimationController LIVING_CONTROLLER = DefaultAnimations.genericLivingController(this);
    private static final RawAnimation BODY_IDLE_ANIMATION = RawAnimation.begin().thenPlay("body.idle");
    private static final RawAnimation BODY_WALK_ANIMATION = RawAnimation.begin().thenPlay("body.walk");
    private static final RawAnimation LEGS_IDLE_ANIMATION = RawAnimation.begin().thenPlay("legs.idle");
    private static final RawAnimation LEGS_WALK_ANIMATION = RawAnimation.begin().thenLoop("legs.walk");
    private static final RawAnimation ARMS_IDLE_ANIMATION = RawAnimation.begin().thenPlay("arms.idle");
    private static final RawAnimation ARMS_WALK_ANIMATION = RawAnimation.begin().thenPlay("arms.walk");
    private static final RawAnimation ARMS_ATTACK_ANIMATION = RawAnimation.begin().thenPlay("arms.attack");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        LIVING_CONTROLLER.setTransitionLength(5);
        controllers.add(
                LIVING_CONTROLLER
                //new AnimationController<>(this, "Legs", 5, this::poseLegs),
                //new AnimationController<>(this, "Body", 5, this::poseBody),
                //new AnimationController<>(this, "Arms", 5, this::poseArms)
        );
    }

    // Create the animation handler for the leg segment
    protected PlayState poseLegs(AnimationState<SculkEndermanEntity> state)
    {
        if(state.isMoving())
        {
            state.setAnimation(LEGS_WALK_ANIMATION);
        }
        else
        {
            //state.setAnimation(LEGS_IDLE_ANIMATION);
            state.setAnimation(LEGS_IDLE_ANIMATION);
        }

        return PlayState.CONTINUE;
    }

    // Create the animation handler for the body segment
    protected PlayState poseBody(AnimationState<SculkEndermanEntity> state)
    {
        if(state.isMoving())
        {
            state.setAnimation(BODY_WALK_ANIMATION);
        }
        else
        {
            state.setAnimation(BODY_IDLE_ANIMATION);
        }

        return PlayState.CONTINUE;
    }

    // Create the animation handler for the arm segment
    protected PlayState poseArms(AnimationState<SculkEndermanEntity> state)
    {
        if(state.getAnimatable().swinging)
        {
            state.setAnimation(ARMS_ATTACK_ANIMATION);
        }
        else if(state.isMoving())
        {
            state.setAnimation(ARMS_WALK_ANIMATION);
        }
        else
        {
            state.setAnimation(ARMS_IDLE_ANIMATION);
        }


        return PlayState.CONTINUE;
    }

    protected float getStandingEyeHeight(Pose p_32517_, EntityDimensions p_32518_) {
        return 2.55F;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ####### Sound Code ###########

    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDERMAN_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.ENDERMAN_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(SoundEvents.ZOMBIE_STEP, 0.15F, 1.0F);
    }

    public boolean dampensVibrations() {
        return true;
    }


    /**
     * If a sculk living entity despawns, refund it's current health to the sculk hoard
     */
    @Override
    public void onRemovedFromWorld() {
        SculkHorde.savedData.addSculkAccumulatedMass((int) this.getHealth());
        super.onRemovedFromWorld();
    }
}
