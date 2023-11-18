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
    }


    @Override
    public void applyEffectTick(LivingEntity sourceEntity, int amp) {

        // If Sculk Living Entity, do damage
        if(EntityAlgorithms.isSculkLivingEntity.test(sourceEntity) || sourceEntity.level().isClientSide())
        {
            return;
        }

        // Create AABB bounding box around entity and check if there are any non-sculk entities inside
        AABB boundingBox = sourceEntity.getBoundingBox();
        boundingBox = boundingBox.inflate(5.0D, 5.0D, 5.0D);
        List<LivingEntity> entities = sourceEntity.level().getEntitiesOfClass(LivingEntity.class, boundingBox);
        entities.removeIf(EntityAlgorithms.isSculkLivingEntity);
        if(!entities.isEmpty())
        {
            // If there are non-sculk entities inside, give them infection.
            // Also damage them and syphon mass from them to give to the horde
            for(LivingEntity e : entities)
            {
                if(e.hasEffect(ModMobEffects.PURITY.get()) || e.getUUID().equals(sourceEntity.getUUID()))
                {
                    continue;
                }
                e.addEffect(new MobEffectInstance(ModMobEffects.DISEASED_CYSTS.get(), TickUnits.convertSecondsToTicks(10), 0));
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

        Random rand = new Random();
        int randomOffset = rand.nextInt(5);
        if(cooldownTicksRemaining + randomOffset > 0)
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
