package com.github.sculkhorde.common.entity.boss.sculk_enderman;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.goal.*;
import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.RaidHandler;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TargetParameters;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
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
    public static final float MAX_HEALTH = 100F;
    //The armor of the mob
    public static final float ARMOR = 20F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 7F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 3F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 64F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.3F;

    // Controls what types of entities this mob can target
    private TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetHostiles().enableTargetInfected().disableBlackListMobs();

    // Timing Variables
    public boolean canTeleport = true;
    protected int TELEPORT_COOLDOWN = TickUnits.convertSecondsToTicks(8);
    protected int ticksSinceLastTeleport = 0;

    protected int SPECIAL_ATTACK_COOLDOWN = TickUnits.convertSecondsToTicks(5);
    protected int ticksSinceLastSpecialAttack = 0;

    protected ServerBossEvent bossEvent;

    protected boolean isInvestigatingPossibleRaidLocation = false;

    // Data
    public static final EntityDataAccessor<Boolean> DATA_ANGRY = SynchedEntityData.defineId(SculkEndermanEntity.class, EntityDataSerializers.BOOLEAN);

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
        this.bossEvent = this.createBossEvent();
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
    }

    public SculkEndermanEntity(Level level, BlockPos pos)
    {
        this(EntityRegistry.SCULK_ENDERMAN.get(), level);
        this.setPos(pos.getX(), pos.getY(), pos.getZ());
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

    public boolean isSpecialAttackReady() {
        return ticksSinceLastSpecialAttack >= SPECIAL_ATTACK_COOLDOWN;
    }

    public void incrementSpecialAttackCooldown() {
        ticksSinceLastSpecialAttack++;
    }

    public void resetSpecialAttackCooldown() {
        ticksSinceLastSpecialAttack = 0;
    }

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

    public boolean isInvestigatingPossibleRaidLocation() {
        return isInvestigatingPossibleRaidLocation;
    }

    public void setInvestigatingPossibleRaidLocation(boolean value)
    {
        isInvestigatingPossibleRaidLocation = value;
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

    public boolean isTeleportCooldownOver() {
        if(getTarget() != null)
        {
            return ticksSinceLastTeleport >= TELEPORT_COOLDOWN/8;
        }

        return ticksSinceLastTeleport >= TELEPORT_COOLDOWN;

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

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new EnderBubbleAttackGoal(this, TickUnits.convertSecondsToTicks(3)));
        this.goalSelector.addGoal(1, new ChaosRiftAttackGoal(this, TickUnits.convertSecondsToTicks(3)));
        this.goalSelector.addGoal(1, new SculkSpineSpikeRadialAttack(this));
        this.goalSelector.addGoal(1, new RainDragonBallAttackGoal(this, TickUnits.convertSecondsToTicks(5)));
        this.goalSelector.addGoal(2, new SummonRandomAttackUnits(this, TickUnits.convertSecondsToTicks(3)));
        this.goalSelector.addGoal(2, new SummonCreepersAttackUnits(this, TickUnits.convertSecondsToTicks(5)));
        this.goalSelector.addGoal(2, new SummonMitesAttackUnits(this, TickUnits.convertSecondsToTicks(3)));
        this.goalSelector.addGoal(3, new RangedDragonBallAttackGoal(this, TickUnits.convertSecondsToTicks(5)));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(4, new PathFindToRaidLocation<>(this));
        this.goalSelector.addGoal(5, new MoveTowardsTargetGoal(this, 0.8F, 20F));
        this.goalSelector.addGoal(6, new ImprovedRandomStrollGoal(this, 1.0D).setToAvoidWater(true));
        this.targetSelector.addGoal(0, new InvalidateTargetGoal(this));
        this.targetSelector.addGoal(1, new TargetAttacker(this));
        this.targetSelector.addGoal(2, new NearestLivingEntityTargetGoal<>(this, false, false));
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount)
    {

        teleportRandomly(32);

        entityData.set(DATA_ANGRY, true);
        return super.hurt(damageSource, amount);
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void aiStep()
    {
        if (this.level().isClientSide) {
            for(int i = 0; i < 2; ++i) {
                this.level().addParticle(ParticleTypes.PORTAL, this.getRandomX(0.5D), this.getRandomY() - 0.25D, this.getRandomZ(0.5D), (this.random.nextDouble() - 0.5D) * 2.0D, -this.random.nextDouble(), (this.random.nextDouble() - 0.5D) * 2.0D);
            }
        }

        this.jumping = false;
        super.aiStep();
    }

    private boolean isWithinRaidLocation()
    {
        return BlockAlgorithms.getBlockDistance(RaidHandler.raidData.getRaidLocation(), this.blockPosition()) <= 64;
    }

    private void teleportTowardsRaidLocationIfOutside()
    {
        if(!isWithinRaidLocation() && isInvestigatingPossibleRaidLocation && isTeleportCooldownOver())
        {
            teleportTowardsPos(RaidHandler.raidData.getRaidLocation());
        }
    }

    public void stayInSpecificRangeOfTarget(int min, int max)
    {
        if(getTarget() == null)
        {
            return;
        }

        if(!isTeleportCooldownOver())
        {
            return;
        }

        // If Too Far, teleport closer
        if(distanceTo(getTarget()) > max)
        {
            teleportTowardsEntity(getTarget());
        }
        // If Too Close, teleport away
        else if(distanceTo(getTarget()) < min)
        {
            teleportAwayFromEntity(getTarget());
        }
    }

    /**
     * Called every tick to update the entity's position/logic.
     */
    protected void customServerAiStep()
    {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        ticksSinceLastTeleport++;

        incrementSpecialAttackCooldown();

        teleportTowardsRaidLocationIfOutside();

        // If angry, dont check for looking players
        if(entityData.get(DATA_ANGRY))
        {
            return;
        }

        // Check to see if any players are looking at the entity
        for (Player player : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(64.0D, 64.0D, 64.0D)))
        {
            if (isLookingAtMe(player))
            {
                // Set angry
                entityData.set(DATA_ANGRY, true);
            }
        }


    }

    /**
     * Teleports the entity to a random position within 64 blocks of the entity
     * @return Returns true if the teleport was successful
     */
    protected boolean teleportRandomly(int distance)
    {
        if (this.level().isClientSide() || !this.isAlive() || !canTeleport)
        {
            return false;
        }

        double d0 = this.getX() + (this.random.nextDouble() - 0.5D) * distance;
        double d1 = this.getY() + (double)(this.random.nextInt(64) - 32);
        double d2 = this.getZ() + (this.random.nextDouble() - 0.5D) * distance;
        return this.teleport(d0, d1, d2);

    }

    /**
     * Teleports the entity towards the given entity
     * @param entity The entity to teleport towards
     * @return Returns true if the teleport was successful
     */
    public boolean teleportTowardsEntity(Entity entity)
    {
        if(!canTeleport)
        {
            return false;
        }

        Vec3 vec3 = new Vec3(this.getX() - entity.getX(), this.getY(0.5D) - entity.getEyeY(), this.getZ() - entity.getZ());
        vec3 = vec3.normalize();
        double teleportDistance = 8.0D;
        double d1 = this.getX() + (this.random.nextDouble() - 0.5D) * 8.0D - vec3.x * teleportDistance;
        double d2 = this.getY() + (double)(this.random.nextInt(16) - 8) - vec3.y * teleportDistance;
        double d3 = this.getZ() + (this.random.nextDouble() - 0.5D) * 8.0D - vec3.z * teleportDistance;
        return this.teleport(d1, d2, d3);
    }

    /**
     * Teleports the entity towards the given entity
     * @param pos The position to teleport towards
     * @return Returns true if the teleport was successful
     */
    protected boolean teleportTowardsPos(BlockPos pos)
    {
        if(!canTeleport)
        {
            return false;
        }

        Vec3 vec3 = new Vec3(this.getX() - pos.getX(), this.getY(0.5D), this.getZ() - pos.getZ());
        vec3 = vec3.normalize();
        double teleportDistance = 8.0D;
        double d1 = this.getX() + (this.random.nextDouble() - 0.5D) * 8.0D - vec3.x * teleportDistance;
        double d2 = this.getY() + (double)(this.random.nextInt(16) - 8) - vec3.y * teleportDistance;
        double d3 = this.getZ() + (this.random.nextDouble() - 0.5D) * 8.0D - vec3.z * teleportDistance;
        return this.teleport(d1, d2, d3);
    }

    /**
     * Teleports the entity away from the given entity
     * @param entity The entity to teleport away from
     * @return Returns true if the teleport was successful
     */
    public boolean teleportAwayFromEntity(Entity entity)
    {
        if(!canTeleport)
        {
            return false;
        }

        Vec3 vec3 = new Vec3(this.getX() - entity.getX(), this.getY(0.5D) - entity.getEyeY(), this.getZ() - entity.getZ());
        vec3 = vec3.normalize();
        double teleportDistance = 8.0D;
        double d1 = this.getX() + (this.random.nextDouble() - 0.5D) * 8.0D + vec3.x * teleportDistance;
        double d2 = this.getY() + (double)(this.random.nextInt(16) - 8) + vec3.y * teleportDistance;
        double d3 = this.getZ() + (this.random.nextDouble() - 0.5D) * 8.0D + vec3.z * teleportDistance;
        return this.teleport(d1, d2, d3);
    }

    /**
     * Teleport the enderman to a random nearby position
     * @param x The x position
     * @param y The y position
     * @param z The z position
     * @return Returns true if the teleport was successful
     */
    protected boolean teleport(double x, double y, double z)
    {
        if(!canTeleport)
        {
            return false;
        }

        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(x, y, z);

        while(blockpos$mutableblockpos.getY() > this.level().getMinBuildHeight() && !this.level().getBlockState(blockpos$mutableblockpos).blocksMotion())
        {
            blockpos$mutableblockpos.move(Direction.DOWN);
        }

        BlockState blockstate = this.level().getBlockState(blockpos$mutableblockpos);
        boolean isMotionBlockFlag = false; blockstate.blocksMotion();
        boolean isWaterFlag = blockstate.getFluidState().is(FluidTags.WATER);
        if (!isWaterFlag)
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
                this.level().gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(this));
                if (!this.isSilent())
                {
                    this.level().playSound((Player)null, this.xo, this.yo, this.zo, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
                    this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                    ticksSinceLastTeleport = 0;
                }
            }

            return ifCanRandomTeleport;
        }
        else
        {
            return false;
        }
    }

    protected ServerBossEvent createBossEvent() {
        ServerBossEvent event = new ServerBossEvent(Component.literal("Sculk Enderman"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
        return event;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
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

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return false; /*source.is(DamageTypeTags.BYPASSES_ARMOR);*/
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(
                LIVING_CONTROLLER.transitionLength(5)
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
