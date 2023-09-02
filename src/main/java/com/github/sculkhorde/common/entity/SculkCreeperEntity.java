package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.goal.*;
import com.github.sculkhorde.common.entity.infection.CursorInfectorEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.TargetParameters;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.concurrent.TimeUnit;

public class SculkCreeperEntity extends Creeper implements ISculkSmartEntity, GeoEntity
{
    private boolean isParticipatingInRaid = false;

    // Controls what types of entities this mob can target
    private TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetHostiles().enableMustReachTarget();

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public SculkCreeperEntity(EntityType<? extends Creeper> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
    }
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new DespawnAfterTime(this, TickUnits.convertMinutesToTicks(15)));
        this.goalSelector.addGoal(0, new DespawnWhenIdle(this, TimeUnit.MINUTES.toSeconds(5)));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SwellGoal(this));
        this.goalSelector.addGoal(2, new BlowUpPriorityBlockGoal(this, 1.0F, 3, 4, 5));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(4, new PathFindToRaidLocation<>(this));
        this.goalSelector.addGoal(5, new ImprovedRandomStrollGoal(this, 1.0D).setToAvoidWater(true));
        this.targetSelector.addGoal(1, new NearestLivingEntityTargetGoal<>(this, true, true));
        this.targetSelector.addGoal(2, new TargetAttacker(this).setAlertAllies());
    }

    @Override
    public void checkDespawn() {}

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

    @Override
    public boolean isIdle() {
        return getTarget() == null;
    }

    public void spawnInfectors()
    {
        int numToSpawn = 15;
        int spawnRange = 5;
        for(int i = 0; i < numToSpawn; i++)
        {
            double x = this.getX() + (this.getRandom().nextDouble() * spawnRange) - spawnRange/2;
            double z = this.getZ() + (this.getRandom().nextDouble() * spawnRange) - spawnRange/2;
            double y = this.getY() + (this.getRandom().nextDouble() * spawnRange/2) - spawnRange/4;
            CursorInfectorEntity infector = new CursorInfectorEntity(ModEntities.CURSOR_INFECTOR.get(), this.level());
            infector.setPos(x, y, z);
            infector.setTickIntervalMilliseconds(3);
            infector.setMaxTransformations(10);
            infector.setMaxRange(10);
            this.level().addFreshEntity(infector);
        }
    }

    @Override
    public void tick() {
        super.tick();
        // The reason I do this is because I need my custom explode function to run before the regular creeper one does.
        // This shit honestly sucks ass, but it works.
        // Creeper expodes when get swelling is 1.1, but if we run it at 1.0, should hopefully work.
        if(getSwelling(1) >= 1.0)
        {
            explodeSculkCreeper();
        }
    }

    public void explodeSculkCreeper()
    {
        if (this.level().isClientSide) {
            return;
        }

        if(!isParticipatingInRaid())
        {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 4.0F, Level.ExplosionInteraction.NONE);
            spawnInfectors();
        }
        else
        {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 4.0F, Level.ExplosionInteraction.MOB);
        }
        this.dead = true;

        this.discard();

    }

    private static final RawAnimation CREEPER_IDLE_ANIMATION = RawAnimation.begin().thenLoop("sculk_creeper.idle");
    private static final RawAnimation BLOB_IDLE_ANIMATION = RawAnimation.begin().thenLoop("sculk_blob.idle");
    private static final RawAnimation CREEPER_WALK_ANIMATION = RawAnimation.begin().thenLoop("sculk_creeper.walk");
    private static final RawAnimation CREEPER_SWELL_ANIMATION = RawAnimation.begin().thenPlay("sculk_creeper.attack");
    private static final RawAnimation CREEPER_DESWELL_ANIMATION = RawAnimation.begin().thenPlay("sculk_creeper.attack.cancel");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "walk_cycle", 5, this::poseWalkCycle),
                new AnimationController<>(this, "attack_cycle", 5, this::poseAttackCycle),
                new AnimationController<>(this, "blob_idle", 5, this::poseBlobIdleCycle)
        );
    }

    protected PlayState poseBlobIdleCycle(AnimationState<SculkCreeperEntity> state)
    {
        state.setAnimation(BLOB_IDLE_ANIMATION);
        return PlayState.CONTINUE;
    }

    // Create the animation handler for the body segment
    protected PlayState poseWalkCycle(AnimationState<SculkCreeperEntity> state)
    {
        if(!state.isMoving())
        {
            state.setAnimation(CREEPER_IDLE_ANIMATION);
        }
        else
        {
            state.setAnimation(CREEPER_WALK_ANIMATION);
        }

        return PlayState.CONTINUE;
    }

    // Create the animation handler for the body segment
    protected PlayState poseAttackCycle(AnimationState<SculkCreeperEntity> state)
    {
        //  Goes from 0 to 30. Vanilla mc mechanics, not mine
        float swellValue = this.getSwelling(1);

        if(this.getSwellDir() > 0)
        {
            state.setAnimation(CREEPER_SWELL_ANIMATION);
            return PlayState.CONTINUE;
        }
        else if(this.getSwellDir() < 0)
        {
            state.setAnimation(CREEPER_DESWELL_ANIMATION);
            return PlayState.CONTINUE;
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
