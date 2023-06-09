package com.github.sculkhorde.util;

import com.github.sculkhorde.common.block.SculkMassBlock;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.Gravemind;
import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.core.EffectRegistry;
import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.gravemind.RaidHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import static com.github.sculkhorde.core.SculkHorde.DEBUG_MODE;

@Mod.EventBusSubscriber(modid = SculkHorde.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventSubscriber {

    private static long time_save_point; //Used to track time passage.
    private static int sculkMassCheck;


    /**
     * This event gets called when a world loads.
     * All we do is initialize the gravemind and some variables
     * used to track changes.
     * @param event The load event
     */
    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event)
    {
        //Initalize Gravemind
        if(!event.getLevel().isClientSide() && event.getLevel().equals(ServerLifecycleHooks.getCurrentServer().overworld()))
        {
            SculkHorde.savedData = ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage().computeIfAbsent(ModSavedData::load, ModSavedData::new, SculkHorde.SAVE_DATA_ID); //Initialize Saved Data
            SculkHorde.gravemind = new Gravemind(); //Initialize Gravemind
            SculkHorde.deathAreaInvestigator = new DeathAreaInvestigator((ServerLevel) event.getLevel()); //Initialize Death Area Investigator
            SculkHorde.raidHandler = new RaidHandler((ServerLevel) event.getLevel()); //Initialize Raid Handler
            time_save_point = 0; //Used to track time passage.
            sculkMassCheck = 0; //Used to track changes in sculk mass
        }
    }

    /**
     * Gets Called Every tick when a world is running.
     * @param event The event with all the details
     */
    @SubscribeEvent
    public static void WorldTickEvent(TickEvent.LevelTickEvent event)
    {

        //Make sure this only gets ran on the server, gravemind has been initalized, and were in the overworld
        if(!event.level.isClientSide() && SculkHorde.gravemind != null && event.level.equals(ServerLifecycleHooks.getCurrentServer().overworld()))
        {
            SculkHorde.savedData.incrementTicksSinceSculkNodeDestruction();

            //Infestation Related Processes
            //Used by anti sculk serum
            SculkHorde.infestationConversionTable.processDeInfectionQueue((ServerLevel) event.level);

            SculkHorde.raidHandler.raidTick(); // Tick the raid handler

            SculkHorde.deathAreaInvestigator.tick(); // Tick the death area investigator


            if (event.level.getGameTime() - time_save_point > TickUnits.convertMinutesToTicks(5))
            {

                time_save_point = event.level.getGameTime();//Set to current time so we can recalculate time passage

                SculkHorde.gravemind.enableAmountOfBeeHives((ServerLevel) event.level, 20);

                //Verification Processes to ensure our data is accurate
                SculkHorde.savedData.validateNodeEntries();
                SculkHorde.savedData.validateBeeNestEntries();

                //Calculate Current State
                SculkHorde.gravemind.calulateCurrentState(); //Have the gravemind update it's state if necessary
                if(DEBUG_MODE) System.out.println("Gravemind Evolution State: " + SculkHorde.gravemind.getEvolutionState().toString());

                if(DEBUG_MODE) System.out.println("Able to Spawn Node?: " + (SculkHorde.savedData.isSculkNodeCooldownOver()));

                //Check How much Mass Was Generated over this period
                if(DEBUG_MODE) System.out.println("Accumulated Mass Since Last Check: " + (SculkHorde.savedData.getSculkAccumulatedMass() - sculkMassCheck));
                sculkMassCheck = SculkHorde.savedData.getSculkAccumulatedMass();

                if(DEBUG_MODE) System.out.println(
                        "\n Known Nodes: " + SculkHorde.savedData.getNodeEntries().size()
                        + "\n Known Nests: " + SculkHorde.savedData.getBeeNestEntries().size()
                        + "\n Known Hostiles: " + SculkHorde.savedData.getHostileEntries().size() + "\n"

                );
                if(DEBUG_MODE) System.out.println("Accumulated Mass Since Last Check: " + (SculkHorde.savedData.getSculkAccumulatedMass() - sculkMassCheck));
            }
        }

    }

    @SubscribeEvent
    public static void onLivingEntityDeathEvent(LivingDeathEvent event)
    {
        if(event.getEntity().level().isClientSide())
        {
            return;
        }

        if(EntityAlgorithms.isSculkLivingEntity.test(event.getEntity()))
        {
            SculkHorde.savedData.reportDeath(event.getEntity().blockPosition());
            SculkHorde.savedData.addHostileToMemory(event.getEntity().getLastHurtByMob());

        }
    }

    @SubscribeEvent
    public static void onPotionExpireEvent(MobEffectEvent.Expired event)
    {
        if(!event.getEntity().level().isClientSide() && SculkHorde.gravemind != null && event.getEntity().level().equals(ServerLifecycleHooks.getCurrentServer().overworld()))
        {
            MobEffectInstance effectInstance = event.getEffectInstance();

            //If Sculk Infection, spawn mites and mass.
            assert effectInstance != null;
            if(effectInstance.getEffect() == EffectRegistry.SCULK_INFECTION.get())
            {
                LivingEntity entity = event.getEntity();
                if(entity != null && entity instanceof LivingEntity)
                {
                    //Spawn Effect Level + 1 number of mites
                    int infectionDamage = 4;
                    Level entityLevel = entity.level();
                    BlockPos entityPosition = entity.blockPosition();
                    float entityHealth = entity.getMaxHealth();

                    //Spawn Mite
                    EntityRegistry.SCULK_MITE.get().spawn((ServerLevel) event.getEntity().level(), entityPosition, MobSpawnType.SPAWNER);

                    //Spawn Sculk Mass
                    SculkMassBlock sculkMass = BlockRegistry.SCULK_MASS.get();
                    sculkMass.spawn(entityLevel, entityPosition, entityHealth);
                    //Do infectionDamage to victim per mite
                    entity.hurt(entity.damageSources().magic(), infectionDamage);
                }
            }
        }
    }
}
