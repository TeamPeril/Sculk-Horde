package com.github.sculkhorde.common.entity.boss.sculk_enderman;

import com.github.sculkhorde.common.entity.boss.SpecialEffectEntity;
import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

/**
 * The following java files were created/edited for this entity.<br>
 * Edited {@link com.github.sculkhorde.core.EntityRegistry}<br>
 * Edited {@link com.github.sculkhorde.client.ClientModEventSubscriber}<br>
 * Added {@link ChaosTeleporationRiftEntity}<br>
 * Added {@link com.github.sculkhorde.client.model.enitity.ChaosTeleporationRiftModel}<br>
 * Added {@link com.github.sculkhorde.client.renderer.entity.ChaosTeleporationRiftRenderer}
 */
public class ChaosTeleporationRiftEntity extends SpecialEffectEntity implements GeoEntity
{
    public static int LIFE_TIME = TickUnits.convertSecondsToTicks(10);
    public int currentLifeTicks = 0;

    public ChaosTeleporationRiftEntity(EntityType<?> entityType, Level level)
    {
        super(entityType, level);
    }

    public ChaosTeleporationRiftEntity(Level level)
    {
        super(EntityRegistry.CHAOS_TELEPORATION_RIFT.get(), level);
    }

    public ChaosTeleporationRiftEntity(EntityType<?> entityType, Level level, LivingEntity sourceEntity) {
        super(entityType, level, sourceEntity);
    }

    public ChaosTeleporationRiftEntity( Level level, LivingEntity sourceEntity) {
        super(EntityRegistry.CHAOS_TELEPORATION_RIFT.get(), level, sourceEntity);
    }

    public ChaosTeleporationRiftEntity enableDeleteAfterTime(int ticks)
    {
        LIFE_TIME = ticks;
        return this;
    }

    @Override
    public void tick() {
        super.tick();

        if(level().isClientSide()) { return; }

        //TODO Uncomment
        //if (sourceEntity == null || !sourceEntity.isAlive()) this.discard();

        currentLifeTicks++;

        // If the entity is alive for more than LIFE_TIME, discard it
        if(currentLifeTicks >= LIFE_TIME && LIFE_TIME != -1) this.discard();


        // Stole this code from Chorus Fruit
        List<LivingEntity> hitList = getEntitiesNearbyCube(LivingEntity.class, 1);
        for (LivingEntity entity : hitList)
        {
            if (entity == sourceEntity)
            {
                continue;
            }

            double d0 = entity.getX();
            double d1 = entity.getY();
            double d2 = entity.getZ();

            for(int i = 0; i < 16; ++i)
            {
                double d3 = entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * 16.0D;
                double d4 = Mth.clamp(entity.getY() + (double)(entity.getRandom().nextInt(16) - 8), (double)level().getMinBuildHeight(), (double)(level().getMinBuildHeight() + ((ServerLevel)level()).getLogicalHeight() - 1));
                double d5 = entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * 16.0D;
                if (entity.isPassenger()) {
                    entity.stopRiding();
                }

                Vec3 vec3 = entity.position();
                level().gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(entity));
                net.minecraftforge.event.entity.EntityTeleportEvent.ChorusFruit event = net.minecraftforge.event.ForgeEventFactory.onChorusFruitTeleport(entity, d3, d4, d5);
                if (event.isCanceled()) return;
                if (entity.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true)) {
                    SoundEvent soundevent = entity instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
                    level().playSound((Player)null, d0, d1, d2, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);
                    entity.playSound(soundevent, 1.0F, 1.0F);
                    entity.hurt(this.damageSources().magic(), 2.0F);
                    // Give entity darkness potion effect
                    entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, TickUnits.convertSecondsToTicks(5), 0));
                    break;
                }
            }
        }


    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    // Data Code

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {

    }

    // Animation Code

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation SPIN_ANIMATION = RawAnimation.begin().thenLoop("misc.idle");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(
                new AnimationController<>(this, "base_animation", 0, this::pose)
        );
    }

    // Create the animation handler for the leg segment
    protected PlayState pose(AnimationState<ChaosTeleporationRiftEntity> state)
    {
        state.setAnimation(SPIN_ANIMATION);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
