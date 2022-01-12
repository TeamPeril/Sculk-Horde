package com.github.sculkhoard.util;

import com.github.sculkhoard.common.entity.SculkMiteEntity;
import com.github.sculkhoard.core.BlockRegistry;
import com.github.sculkhoard.core.EffectRegistry;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SculkHoard.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventSubscriber {

    @SubscribeEvent
    public static void onPotionExpireEvent(PotionEvent.PotionExpiryEvent event)
    {
        EffectInstance effectInstance = event.getPotionEffect();
        if(effectInstance.getEffect() == EffectRegistry.SCULK_INFECTION.get())
        {
            LivingEntity entity = event.getEntityLiving();
            if(entity != null)
            {

                //Spawn Effect Level + 1 number of mites
                int infectionDamage = 4;
                for(int i = 0; i < effectInstance.getAmplifier() + 1; i++)
                {
                    SculkMiteEntity mite = new SculkMiteEntity(entity.level);
                    mite.setPos(entity.getX(), entity.getY(), entity.getZ());
                    entity.level.addFreshEntity(mite);
                    if(entity.getHealth() <= infectionDamage)
                    {
                        entity.level.setBlockAndUpdate(entity.blockPosition(), BlockRegistry.SCULK_MASS.get().defaultBlockState());
                    }
                    entity.hurt(DamageSource.GENERIC, infectionDamage); //Do 2 hearts of damage per mite
                }
            }
        }
    }
}
