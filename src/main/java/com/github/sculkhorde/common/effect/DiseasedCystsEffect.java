package com.github.sculkhorde.common.effect;

import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DiseasedCystsEffect extends MobEffect {

    public static int liquidColor = 338997;
    public static MobEffectCategory effectType = MobEffectCategory.HARMFUL;
    public long COOLDOWN = TickUnits.convertSecondsToTicks(5);
    public long cooldownTicksRemaining = COOLDOWN;
    private Random random = new Random();

    private int randomApplyEffectOffset;


    /**
     * Old Dumb Constructor
     * @param effectType Determines if harmful or not
     * @param liquidColor The color in some number format
     */
    protected DiseasedCystsEffect(MobEffectCategory effectType, int liquidColor) {
        super(effectType, liquidColor);
    }

    /**
     * Simpler Constructor
     */
    public DiseasedCystsEffect() {
        this(effectType, liquidColor);
        randomApplyEffectOffset = random.nextInt(TickUnits.convertSecondsToTicks(2));
    }


    @Override
    public void applyEffectTick(LivingEntity sourceEntity, int amp) {

        if(sourceEntity.level().isClientSide())
        {
            return;
        }

        // Create AABB bounding box around entity and check if there are any non-sculk entities inside
        AABB boundingBox = sourceEntity.getBoundingBox();
        boundingBox = boundingBox.inflate(15.0D, 15.0D, 15.0D);
        List<LivingEntity> entities = sourceEntity.level().getEntitiesOfClass(LivingEntity.class, boundingBox);
        if(!entities.isEmpty())
        {
            // If there are non-sculk entities inside, give them infection.
            // Also damage them and syphon mass from them to give to the horde
            for(LivingEntity e : entities)
            {
                if(EntityAlgorithms.isLivingEntityExplicitDenyTarget(e))
                {
                    continue;
                }

                if(e.hasEffect(ModMobEffects.PURITY.get()))
                {
                    // Remove 20 seconds from the purity effect
                    long oldDuration = e.getEffect(ModMobEffects.PURITY.get()).getDuration();
                    int oldAmplifier = e.getEffect(ModMobEffects.PURITY.get()).getAmplifier();
                    long newDuration = Math.max(oldDuration - TickUnits.convertSecondsToTicks(5),1);
                    e.removeEffect(ModMobEffects.PURITY.get());
                    e.addEffect(new MobEffectInstance(ModMobEffects.PURITY.get(), (int)newDuration, oldAmplifier));
                }
                if(!e.hasEffect(ModMobEffects.SCULK_INFECTION.get()))
                {
                    e.addEffect(new MobEffectInstance(ModMobEffects.SCULK_INFECTION.get(), TickUnits.convertSecondsToTicks(20), 0));
                }

                if(e.getHealth() <= e.getMaxHealth() * 0.5)
                {
                    continue;
                }
                e.hurtMarked = true;
                int damage = (int) (e.getMaxHealth() * 0.1F);
                e.hurt(e.damageSources().generic(), damage);
                SculkHorde.savedData.addSculkAccumulatedMass(damage);
                SculkHorde.statisticsData.addTotalMassFromDiseasedCysts(damage);
            }
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
        cooldownTicksRemaining = COOLDOWN + randomApplyEffectOffset;
        return true;

    }

    @Override
    public List<ItemStack> getCurativeItems() {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
        return ret;
    }

}
