package com.github.sculkhorde.common.entity.specialeffects;

import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

/**
 * The following java files were created/edited for this entity.<br>
 * Edited {@link com.github.sculkhorde.core.EntityRegistry}<br>
 * Edited {@link com.github.sculkhorde.client.ClientModEventSubscriber}<br>
 * Added {@link EnderBubbleAttackEntity}<br>
 * Added {@link com.github.sculkhorde.client.model.enitity.EnderBubbleAttackModel}<br>
 * Added {@link com.github.sculkhorde.client.renderer.entity.EnderBubbleAttackRenderer}
 */
public class EnderBubbleAttackEntity extends SpecialEffectEntity implements GeoEntity {

    public static int LIFE_TIME = TickUnits.convertSecondsToTicks(10);
    public int currentLifeTicks = 0;

    public EnderBubbleAttackEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public EnderBubbleAttackEntity( Level level) {
        super(EntityRegistry.ENDER_BUBBLE_ATTACK.get(), level);
    }

    public EnderBubbleAttackEntity(EntityType<?> entityType, Level level, LivingEntity sourceEntity) {
        super(entityType, level, sourceEntity);
    }

    public EnderBubbleAttackEntity( Level level, LivingEntity sourceEntity) {
        super(EntityRegistry.ENDER_BUBBLE_ATTACK.get(), level, sourceEntity);
    }

    public EnderBubbleAttackEntity enableDeleteAfterTime(int ticks)
    {
        LIFE_TIME = ticks;
        return this;
    }

    @Override
    public void tick() {
        super.tick();
        //TODO Uncomment
        //if (sourceEntity == null || !sourceEntity.isAlive()) this.discard();

        currentLifeTicks++;

        // If the entity is alive for more than LIFE_TIME, discard it
        if(currentLifeTicks >= LIFE_TIME && LIFE_TIME != -1) this.discard();

        //playSound(SoundEvents.GENERIC_EXPLODE);


        List<LivingEntity> hitList = getEntitiesNearbyCube(LivingEntity.class, 3);
        for (LivingEntity entity : hitList)
        {
            if (entity == sourceEntity || EntityAlgorithms.isSculkLivingEntity.test(entity))
            {
                continue;
            }

            //TODO Uncomment when source entity actually works
            //entity.hurt(damageSources().indirectMagic(entity, sourceEntity), 1);
            entity.hurt(damageSources().generic(), 1);
        }


    }

    // ### Data ###

    @Override
    protected void defineSynchedData() {

    }

    // ### GECKOLIB Animation Code ###
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericIdleController(this));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
