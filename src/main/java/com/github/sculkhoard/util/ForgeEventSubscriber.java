package com.github.sculkhoard.util;

import com.github.sculkhoard.common.block.SculkMassBlock;
import com.github.sculkhoard.common.entity.SculkMiteEntity;
import com.github.sculkhoard.core.BlockRegistry;
import com.github.sculkhoard.core.EffectRegistry;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SculkHoard.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventSubscriber {

    @SubscribeEvent
    public static void onPotionExpireEvent(PotionEvent.PotionExpiryEvent event)
    {
        EffectInstance effectInstance = event.getPotionEffect();

        /**
         * If Sculk Infection, spawn mites and mass.
         */
        if(effectInstance.getEffect() == EffectRegistry.SCULK_INFECTION.get())
        {
            LivingEntity entity = event.getEntityLiving();
            if(entity != null)
            {
                //Spawn Effect Level + 1 number of mites
                int infectionDamage = 4;
                for(int i = 0; i < effectInstance.getAmplifier() + 1; i++)
                {
                    World entityLevel = entity.level;
                    BlockPos entityPosition = entity.blockPosition();
                    float entityHealth = entity.getMaxHealth();

                    SculkMiteEntity mite = new SculkMiteEntity(entityLevel);
                    mite.setPos(entity.getX(), entity.getY(), entity.getZ());
                    entityLevel.addFreshEntity(mite);

                    //Spawn Sculk Mass
                    SculkMassBlock sculkMass = BlockRegistry.SCULK_MASS.get();
                    sculkMass.spawn(entityLevel, entityPosition, entityHealth);
                    //Do infectionDamage to victim per mite
                    entity.hurt(DamageSource.GENERIC, infectionDamage);
                }
            }
        }
    }


}
