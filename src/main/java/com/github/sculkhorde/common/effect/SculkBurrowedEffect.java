package com.github.sculkhorde.common.effect;

import com.github.sculkhorde.common.block.SculkMassBlock;
import com.github.sculkhorde.core.*;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.entity.living.MobEffectEvent;

import java.util.ArrayList;
import java.util.List;

public class SculkBurrowedEffect extends MobEffect {

    public static int spawnInterval = 20;
    public static int liquidColor = 338997;
    public static MobEffectCategory effectType = MobEffectCategory.HARMFUL;

    public long COOLDOWN = TickUnits.convertSecondsToTicks(1);
    public long cooldownTicksRemaining = COOLDOWN;


    /**
     * Old Dumb Constructor
     * @param effectType Determines if harmful or not
     * @param liquidColor The color in some number format
     */
    protected SculkBurrowedEffect(MobEffectCategory effectType, int liquidColor) {
        super(effectType, liquidColor);
    }

    /**
     * Simpler Constructor
     */
    public SculkBurrowedEffect() {
        this(effectType, liquidColor);
    }

    public static void onPotionExpire(MobEffectEvent.Expired event)
    {
        if(event.getEntity().level.isClientSide()) { return;}

        LivingEntity entity = event.getEntity();
        // OR mob outside of world border
        if(entity == null || EntityAlgorithms.isSculkLivingEntity.test(entity) || !entity.level.isInWorldBounds(entity.blockPosition()))
        {
            return;
        }

        //Spawn Effect Level + 1 number of mites
        int infectionDamage = 4;
        BlockPos entityPosition = entity.blockPosition();

        //Spawn Mite
        if(entity.level.getFluidState(entityPosition) == Fluids.EMPTY.defaultFluidState() || !ModConfig.isExperimentalFeaturesEnabled()) { ModEntities.SCULK_MITE.get().spawn((ServerLevel) event.getEntity().level, (ItemStack)null, (Player)null, entityPosition, MobSpawnType.SPAWNER, false, false); }
        else { ModEntities.SCULK_SALMON.get().spawn((ServerLevel) event.getEntity().level, (ItemStack)null, (Player)null, entityPosition, MobSpawnType.SPAWNER, false, false); }

        //Spawn Sculk Mass
        placeSculkMass(entity);
        //Do infectionDamage to victim per mite
        entity.hurt(DamageSource.MAGIC, infectionDamage);
        // I eated ParticleUtil
        //ParticleUtil.spawnBurrowedBurstParticles((ServerLevel) entity.level, entity.position().add(0, 0.66F, 0).toVector3f(), 12, 0.2F);
        //((ServerLevel) entity.level()).playSound(entity, entity.blockPosition(), ModSounds.BURROWED_BURST.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
        entity.level.playSound((Player)null, entity.blockPosition().getX(), entity.blockPosition().getY(), entity.blockPosition().getZ(), ModSounds.BURROWED_BURST.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    public static void placeSculkMass(LivingEntity entity)
    {
        int maxDistanceToCheck = 30;
        BlockPos placementPos = entity.blockPosition();

        for(int i = 0; i < maxDistanceToCheck; i++)
        {
            if(entity.level.getBlockState(placementPos.below()).canBeReplaced(Fluids.WATER))
            {
                placementPos = placementPos.below();
            }
        }
        SculkMassBlock sculkMass = ModBlocks.SCULK_MASS.get();
        sculkMass.spawn(entity.level, placementPos.below(), entity.getMaxHealth());

    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if(entity.level.isClientSide()) { return;}
        if(EntityAlgorithms.isSculkLivingEntity.test(entity))
        {
            // Remove effect
            entity.removeEffect(ModMobEffects.SCULK_INFECTION.get());
        }
    }

    /**
     * A function that is called every tick an entity has this effect. <br>
     * I do not use because it does not provide any useful inputs like
     * the entity it is affecting. <br>
     * I instead use ForgeEventSubscriber.java to handle the logic.
     * @param ticksLeft The amount of ticks remaining
     * @param amplifier The level of the effect
     * @return Determines if the effect should apply.
     */
    @Override
    public boolean isDurationEffectTick(int ticksLeft, int amplifier) {

        if(cooldownTicksRemaining > 0)
        {
            cooldownTicksRemaining--;
            return false;
        }
        cooldownTicksRemaining = COOLDOWN;
        return true;

    }

    @Override
    public List<ItemStack> getCurativeItems() {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
        return ret;
    }


}
