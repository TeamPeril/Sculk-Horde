package com.github.sculkhorde.util;

import com.github.sculkhorde.common.effect.SculkInfectionEffect;
import com.github.sculkhorde.core.ModItems;
import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.Gravemind;
import com.github.sculkhorde.core.gravemind.RaidHandler;
import com.github.sculkhorde.core.gravemind.SculkNodesHandler;
import com.github.sculkhorde.util.ChunkLoading.BlockEntityChunkLoaderHelper;
import com.github.sculkhorde.util.ChunkLoading.EntityChunkLoaderHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;


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
            SculkHorde.deathAreaInvestigator = new DeathAreaInvestigator(); //Initialize Death Area Investigator
            SculkHorde.raidHandler = new RaidHandler((ServerLevel) event.getLevel()); //Initialize Raid Handler
            SculkHorde.sculkNodesHandler = new SculkNodesHandler(); //Initialize Sculk Nodes Handler
            SculkHorde.entityChunkLoaderHelper = new EntityChunkLoaderHelper(); //Initialize Entity Chunk Loader Helper
            SculkHorde.blockEntityChunkLoaderHelper = new BlockEntityChunkLoaderHelper(); //Initialize Block Entity Chunk Loader Helper
            if(SculkHorde.statisticsData == null)
            {
                SculkHorde.statisticsData = new StatisticsData();
            }
            time_save_point = 0; //Used to track time passage.
            sculkMassCheck = 0; //Used to track changes in sculk mass

            // Check if chunk 0,0 is loaded. If not, load it.
            if(!event.getLevel().getChunkSource().hasChunk(0,0))
            {
                BlockEntityChunkLoaderHelper.getChunkLoaderHelper().createChunkLoadRequestSquare(((ServerLevel)event.getLevel()), BlockPos.ZERO, 5, 0, TickUnits.convertMinutesToTicks(10));
            }
        }
    }

    /**
     * Gets Called Every tick when a world is running.
     * @param event The event with all the details
     */
    @SubscribeEvent
    public static void WorldTickEvent(TickEvent.LevelTickEvent event)
    {
        // If we are on client or the gravemind is null or we are not in the overworld, return
        if(event.level.isClientSide() || SculkHorde.gravemind == null || !event.level.equals(ServerLifecycleHooks.getCurrentServer().overworld()))
        {
            return;
        }

        // Run this stuff every tick

        SculkHorde.savedData.incrementNoNodeSpawningTicksElapsed();

        SculkHorde.raidHandler.raidTick(); // Tick the raid handler
        SculkHorde.deathAreaInvestigator.tick();
        SculkHorde.sculkNodesHandler.tick();

        SculkHorde.blockEntityChunkLoaderHelper.processBlockChunkLoadRequests();
        SculkHorde.entityChunkLoaderHelper.processEntityChunkLoadRequests();

        // Only run stuff below every 5 minutes
        if (event.level.getGameTime() - time_save_point < TickUnits.convertMinutesToTicks(5))
        {
            return;
        }

        time_save_point = event.level.getGameTime();//Set to current time so we can recalculate time passage
        SculkHorde.gravemind.enableAmountOfBeeHives(20);

        //Verification Processes to ensure our data is accurate
        SculkHorde.savedData.validateNodeEntries();
        SculkHorde.savedData.validateBeeNestEntries();
        SculkHorde.savedData.validateNoRaidZoneEntries();
        SculkHorde.savedData.validateAreasOfInterest();

        //Calculate Current State
        SculkHorde.gravemind.calulateCurrentState(); //Have the gravemind update it's state if necessary

        //Check How much Mass Was Generated over this period
        if(SculkHorde.isDebugMode()) System.out.println("Accumulated Mass Since Last Check: " + (SculkHorde.savedData.getSculkAccumulatedMass() - sculkMassCheck));
        sculkMassCheck = SculkHorde.savedData.getSculkAccumulatedMass();

    }

    @SubscribeEvent
    public static void onLivingEntityDeathEvent(LivingDeathEvent event)
    {
        if(event.getEntity().level.isClientSide())
        {
            return;
        }

        if(EntityAlgorithms.isSculkLivingEntity.test(event.getEntity()))
        {
            SculkHorde.savedData.reportDeath((ServerLevel) event.getEntity().level, event.getEntity().blockPosition());
            SculkHorde.savedData.addHostileToMemory(event.getEntity().getLastHurtByMob());

        }
    }

    @SubscribeEvent
    public static void onPotionExpireEvent(MobEffectEvent.Expired event)
    {
        if(event.getEntity().level.isClientSide() || SculkHorde.gravemind == null)
        {
            return;
        }

        MobEffectInstance effectInstance = event.getEffectInstance();

        if(effectInstance == null)
        {
            return;
        }

        if(effectInstance.getEffect() == ModMobEffects.SCULK_INFECTION.get())
        {
            SculkInfectionEffect.onPotionExpire(event);
        }

    }

    @SubscribeEvent
    public static void OnLivingDamageEvent(LivingDamageEvent event)
    {
        // Get Item being used to attack
        ItemStack itemStack = ItemStack.EMPTY;
        Entity damageSourceEntity = event.getSource().getEntity();
        LivingEntity targetEntity = event.getEntity();
        if(damageSourceEntity instanceof LivingEntity attackingEntity)
        {
            itemStack = attackingEntity.getMainHandItem();
            if(!itemStack.getItem().equals(ModItems.SCULK_SWEEPER_SWORD.get()))
            {
               return;
            }

            if(!EntityAlgorithms.isSculkLivingEntity.test(targetEntity))
            {
                event.setAmount(event.getAmount()/2);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.player.level.isClientSide())
        {
            return;
        }

        if(event.player.tickCount % 20 == 0)
        {
            AdvancementUtil.advancementHandlingTick((ServerLevel) event.player.level);
        }
    }
}
