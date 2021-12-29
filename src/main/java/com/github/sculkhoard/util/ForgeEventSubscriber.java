package com.github.sculkhoard.util;

import com.github.sculkhoard.common.entity.SculkMiteEntity;
import com.github.sculkhoard.core.EffectRegistry;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SculkHoard.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventSubscriber {

    @SubscribeEvent
    public static void onPotionRemoveEvent(PotionEvent.PotionRemoveEvent event)
    {
        EffectInstance effectInstance = event.getPotionEffect();
        if(event.getPotion() == EffectRegistry.SCULK_INFECTION.get())
        {
            LivingEntity entity = event.getEntityLiving();
            if(entity != null)
            {
                //Spawn Effect Level + 1 number of mites
                for(int i = 0; i < effectInstance.getAmplifier() + 1; i++)
                {
                    SculkMiteEntity mite = new SculkMiteEntity(entity.level);
                    mite.setPos(entity.getX(), entity.getY(), entity.getZ());
                    entity.level.addFreshEntity(mite);
                }
            }
        }
    }
}
