package com.github.sculkhorde.common.effect;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

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

        if(sourceEntity.level.isClientSide())
        {
            return;
        }

        // Create AABB bounding box around entity and check if there are any non-sculk entities inside
        AABB boundingBox = sourceEntity.getBoundingBox();
        boundingBox = boundingBox.inflate(10.0D, 10.0D, 10.0D);
        List<LivingEntity> entities = sourceEntity.level.getEntitiesOfClass(LivingEntity.class, boundingBox);
        if(!entities.isEmpty())
        {
            // If there are non-sculk entities inside, give them infection.
            // Also damage them and syphon mass from them to give to the horde
            for(LivingEntity victim : entities)
            {
                if(EntityAlgorithms.isLivingEntityExplicitDenyTarget(victim))
                {
                    continue;
                }

                EntityAlgorithms.reducePurityEffectDuration(victim, TickUnits.convertSecondsToTicks(60));
                EntityAlgorithms.applyDebuffEffect(victim, ModMobEffects.SCULK_INFECTION.get(), TickUnits.convertSecondsToTicks(10), 0);

                if(victim.getHealth() <= victim.getMaxHealth() * 0.4)
                {
                    continue;
                }
                victim.hurtMarked = true;
                int damage = (int) (victim.getMaxHealth() * 0.1F);
                victim.hurt(DamageSource.GENERIC, damage);
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
