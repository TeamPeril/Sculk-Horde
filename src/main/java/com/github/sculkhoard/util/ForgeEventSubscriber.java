package com.github.sculkhoard.util;

import com.github.sculkhoard.common.block.SculkMassBlock;
import com.github.sculkhoard.common.entity.SculkMiteEntity;
import com.github.sculkhoard.common.entity.gravemind.Gravemind;
import com.github.sculkhoard.core.BlockRegistry;
import com.github.sculkhoard.core.EffectRegistry;
import com.github.sculkhoard.core.EntityRegistry;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import static com.github.sculkhoard.core.SculkHoard.DEBUG_MODE;
import static com.github.sculkhoard.core.SculkHoard.gravemind;

@Mod.EventBusSubscriber(modid = SculkHoard.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventSubscriber {

    private static long time_save_point = 0; //Used to track time passage.

    //This occurs when a world is loaded
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event)
    {
        //Initalize Gravemind
        gravemind = new Gravemind();
        if(DEBUG_MODE && SculkHoard.gravemind != null) System.out.println("Gravemind Loaded");
        else if(DEBUG_MODE && SculkHoard.gravemind == null) System.out.println("Gravemind is NULL");
    }

    /**
     * Gets Called Every tick when a world is running.
     * @param event The event with all the details
     */
    @SubscribeEvent
    public static void WorldTickEvent(TickEvent.WorldTickEvent event)
    {
        int ticks_per_second = 20; //Unit is ticks
        int seconds_between_intervals = 60; //Unit is Seconds

        //Every 'seconds_between_intervals' amount of seconds, check the gravemind state.
        if(event.world.getGameTime() - time_save_point > seconds_between_intervals * ticks_per_second)
        {
            time_save_point = event.world.getGameTime();//Set to current time so we can recalculate time passage
            gravemind.deductCurrentState(); //Have the gravemind update it's state if necessary
            if(DEBUG_MODE) System.out.println("Gravemind Evolution State: " + gravemind.getEvolutionState().toString());
        }
    }

    @SubscribeEvent
    public static void onPotionExpireEvent(PotionEvent.PotionExpiryEvent event)
    {
        if(!event.getEntity().level.isClientSide())
        {
            EffectInstance effectInstance = event.getPotionEffect();

            //If Sculk Infection, spawn mites and mass.
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

                        //Spawn Mite
                        EntityRegistry.SCULK_MITE.get().spawn((ServerWorld) event.getEntity().level, null, null, entityPosition, SpawnReason.SPAWNER, true, true);

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
}
