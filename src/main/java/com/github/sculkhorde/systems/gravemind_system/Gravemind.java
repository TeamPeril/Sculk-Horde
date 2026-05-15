package com.github.sculkhorde.systems.gravemind_system;


import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.debugger_system.DebuggerSystem;
import com.github.sculkhorde.systems.gravemind_system.entity_factory.EntityFactory;
import com.github.sculkhorde.systems.gravemind_system.entity_factory.ReinforcementRequest;
import com.github.sculkhorde.util.ChunkLoading.BlockEntityChunkLoaderHelper;
import com.github.sculkhorde.util.MobProfileUtil;
import com.github.sculkhorde.util.WardZoneUtil;
import com.github.sculkhorde.util.PlayerProfileHandler;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraftforge.server.ServerLifecycleHooks;

import static com.github.sculkhorde.systems.gravemind_system.entity_factory.EntityFactoryEntry.StrategicValues.Combat;
import static com.github.sculkhorde.systems.gravemind_system.entity_factory.EntityFactoryEntry.StrategicValues.Infector;

/**
 * This class represents the logistics for the Gravemind and is SEPARATE from the physical version.
 * The gravemind is a state machine that is used to coordinate the sculk hoard.
 * Right now only controls the reinforcement system.
 *
 * Future Plans:
 * -Controls Sculk Raids
 * -Coordinate Defense
 * -Make Coordination of Reinforcements more advanced
 */
public class Gravemind
{
    public static enum evolution_states {Undeveloped, Immature, Mature}

    private evolution_states evolution_state;

    public static EntityFactory entityFactory;

    public static final int MINIMUM_DISTANCE_BETWEEN_NODES = 300;
    public int sculk_node_limit = 1;
    public static int TICKS_BETWEEN_NODE_SPAWNS = TickUnits.convertMinutesToTicks(ModConfig.SERVER.sculk_node_spawn_cooldown_minutes.get());
    private static long time_save_point = 0; //Used to track time passage.

    protected long timeOfLastChunkLoadAttempt = 0;
    protected long CHUNK_LOAD_ATTEMPT_COOLDOWN = TickUnits.convertMinutesToTicks(2);


    public boolean isWorldFullyLoaded = false;

    /**
     * Default Constructor <br>
     * Called in ForgeEventSubscriber.java in world load event. <br>
     * WARNING: DO NOT CALL THIS FUNCTION UNLESS THE WORLD IS LOADED
     */
    public Gravemind()
    {
        evolution_state = evolution_states.Undeveloped;
        entityFactory = SculkHorde.entityFactory;
    }





    // Accessors

    public static boolean isGravemindActive()
    {
        if(SculkHorde.gravemind == null)
        {
            return false;
        }

        if(PlayerProfileHandler.arePlayersOfflineAndSpreadingOfflineDisabled())
        {
            return false;
        }

        return SculkHorde.gravemind.isWorldFullyLoaded();
    }

    public boolean isWorldFullyLoaded()
    {
        return isWorldFullyLoaded;
    }

    public evolution_states getEvolutionState()
    {
        return evolution_state;
    }

    /**
     * Used to figure out what state the gravemind is in. Called periodically. <br>
     * Useful for when world is loaded in because we dont store the state.
     */
    public void calulateCurrentState()
    {

        //This is how much mass is needed to go from undeveloped to immature
        int MASS_GOAL_FOR_IMMATURE = ModConfig.SERVER.gravemind_mass_goal_for_immature_stage.get();
        //This is how much mass is needed to go from immature to mature
        int MASS_GOAL_FOR_MATURE = ModConfig.SERVER.gravemind_mass_goal_for_mature_stage.get();

        if(ModSavedData.getSaveData().getSculkAccumulatedMass() >= MASS_GOAL_FOR_MATURE)
        {
            evolution_state = evolution_states.Mature;
            sculk_node_limit = 8;
        }
        else if(ModSavedData.getSaveData().getSculkAccumulatedMass() >= MASS_GOAL_FOR_IMMATURE)
        {
            evolution_state = evolution_states.Immature;
            sculk_node_limit = 4;
            if(ModSavedData.getSaveData().isHordeUnactivated())
            {
                ModSavedData.getSaveData().setHordeState(ModSavedData.HordeState.ACTIVE);
            }
        }

    }

    public void advanceState()
    {
        if(evolution_state == evolution_states.Undeveloped)
        {
            ModSavedData.getSaveData().setSculkAccumulatedMass(ModConfig.SERVER.gravemind_mass_goal_for_immature_stage.get());
            calulateCurrentState();
        }
        else if(evolution_state == evolution_states.Immature)
        {
            ModSavedData.getSaveData().setSculkAccumulatedMass(ModConfig.SERVER.gravemind_mass_goal_for_mature_stage.get());
            calulateCurrentState();
        }
    }

    public void deadvanceState()
    {
        if(evolution_state == evolution_states.Immature)
        {
            ModSavedData.getSaveData().setSculkAccumulatedMass(ModConfig.SERVER.gravemind_mass_goal_for_immature_stage.get()/2);
        }
        else if(evolution_state == evolution_states.Mature)
        {
            ModSavedData.getSaveData().setSculkAccumulatedMass(ModConfig.SERVER.gravemind_mass_goal_for_mature_stage.get()/2);

        }
        calulateCurrentState();
    }

    public void resetGravemindState()
    {
        ModSavedData.getSaveData().setSculkAccumulatedMass(0);
        calulateCurrentState();
    }


    public void processReinforcementRequest(ReinforcementRequest context)
    {
        context.isRequestViewed = true;


        boolean isSenderDeveloper = context.sender == ReinforcementRequest.senderType.Developer;
        boolean isSenderSculkMassBlock = context.sender == ReinforcementRequest.senderType.SculkMass;
        boolean isThereNoMass = ModSavedData.getSaveData().getSculkAccumulatedMass() <= 0;
        boolean isHordeDefeated = ModSavedData.getSaveData().isHordeDefeated();

        //Auto approve is this reinforcement is requested by a developer or sculk mass
        if(isSenderDeveloper || isSenderSculkMassBlock)
        {
            context.isRequestApproved = true;
        }

        if(isHordeDefeated || isThereNoMass)
        {
            return;
        }

        boolean isSenderTypeSummoner = context.sender == ReinforcementRequest.senderType.Summoner;
        boolean isSenderTypeMassBlock = context.sender == ReinforcementRequest.senderType.SculkMass;
        boolean isThereAtLeastOneSpawnPoint = context.positions.length > 0;

        if( (isSenderTypeSummoner || isSenderTypeMassBlock) && isThereAtLeastOneSpawnPoint)
        {
            if(SculkHorde.populationHandler.isPopulationAtMax())
            {
                context.isRequestApproved = false;
                return;
            }
        }

        //Spawn Combat Mobs to deal with player
        if(context.is_aggressor_nearby)
        {
            context.approvedStrategicValues.add(Combat);
            context.isRequestApproved = true;
        }

        //Spawn infector mobs to infect
        //NOTE: I turned this into an else if because is both aggressors and passives are present,
        //it will choose from both combat and infector units. I think its better we prioritize
        //spawning aggressors if both are present
        else if(context.is_non_sculk_mob_nearby)
        {
            context.approvedStrategicValues.add(Infector);
            context.isRequestApproved = true;
        }
    }

    /**
     * Determines if a given evolution state is equal to or below the current evolution state.
     * @param stateIn The given state to check
     * @return True if the given state is equal to or less than current evolution state.
     */
    public boolean isEvolutionStateEqualOrLessThanCurrent(evolution_states stateIn)
    {
        if(evolution_state == evolution_states.Undeveloped)
        {
            return (stateIn == evolution_states.Undeveloped);
        }
        else if(evolution_state == evolution_states.Immature)
        {
            return (stateIn == evolution_states.Immature || stateIn == evolution_states.Undeveloped);
        }
        else if(evolution_state == evolution_states.Mature)
        {
            return(stateIn == evolution_states.Undeveloped
                || stateIn == evolution_states.Immature
                || stateIn == evolution_states.Mature);
        }
        return false;
    }

    public boolean isEvolutionInMatureState()
    {
        return evolution_state == evolution_states.Mature;
    }

    public boolean isEvolutionInImmatureStateOrAbove()
    {
        return evolution_state == evolution_states.Immature || evolution_state == evolution_states.Mature;
    }

    public int getPotionAmplificationBasedOnGravemindState()
    {
        if(evolution_state == evolution_states.Undeveloped)
        {
            return 0;
        }
        else if(evolution_state == evolution_states.Immature)
        {
            return 1;
        }
        else if(evolution_state == evolution_states.Mature)
        {
            return 2;
        }
        else
        {
            return 3;
        }
    }

    // EVENTS

    public void serverTick()
    {
        /*  The reason we wait a minute after the server starts is due to a weird issue I experienced when developing the
            virtual cursor system. For some reason, the game will randomly stall upon generating a world at around 99%.
         */
        if(!isGravemindActive())
        {
            return;
        }

        calulateCurrentState();

        // Run this stuff every tick

        ModSavedData.getSaveData().incrementNoNodeSpawningTicksElapsed();

        SculkHorde.deathAreaInvestigator.tick();
        SculkHorde.sculkNodesSystem.tick();
        SculkHorde.eventSystem.serverTick();
        SculkHorde.cursorSystem.serverTick();
        SculkHorde.populationHandler.serverTick();
        SculkHorde.blockEntityChunkLoaderHelper.processBlockChunkLoadRequests();
        SculkHorde.entityChunkLoaderHelper.processEntityChunkLoadRequests();
        SculkHorde.beeNestActivitySystem.serverTick();
        SculkHorde.chunkInfestationSystem.serverTick();
        SculkHorde.pathBuilderSystem.serverTick();
        SculkHorde.autoPerformanceSystem.onServerTick();
        SculkHorde.ambientSFXSystem.serverTick();
        SculkHorde.squadSystem.serverTick();
        SculkHorde.debuggerSystem.serverTick();
        SculkHorde.hitSquadDispatcherSystem.serverTick();


        // Make sure the area above the tomb is loaded. Only attempt every CHUNK_LOAD_ATTEMPT_COOLDOWN
        if(ServerLifecycleHooks.getCurrentServer().overworld().getGameTime() - timeOfLastChunkLoadAttempt >= CHUNK_LOAD_ATTEMPT_COOLDOWN)
        {
            if(!ServerLifecycleHooks.getCurrentServer().overworld().getChunkSource().hasChunk(0,0))
            {
                DebuggerSystem.chunkLoaderDebuggerModule.logInfo("Gravemind | Loading Chunk Area at Sculk Tomb.");
                BlockEntityChunkLoaderHelper.getChunkLoaderHelper().createChunkLoadRequestSquare((ServerLifecycleHooks.getCurrentServer().overworld()), BlockPos.ZERO, 5, 0, TickUnits.convertMinutesToTicks(10));
                DebuggerSystem.chunkLoaderDebuggerModule.logInfo("Gravemind | Loaded Chunk Area at Sculk Tomb.");
            }
            timeOfLastChunkLoadAttempt = ServerLifecycleHooks.getCurrentServer().overworld().getGameTime();
        }

        // Only run stuff below every 5 minutes
        if (ServerLifecycleHooks.getCurrentServer().overworld().getGameTime() - time_save_point < TickUnits.convertMinutesToTicks(5))
        {
            return;
        }

        time_save_point = ServerLifecycleHooks.getCurrentServer().overworld().getGameTime();//Set to current time so we can recalculate time passage
        SculkHorde.beeNestActivitySystem.activate();

        //Verification Processes to ensure our data is accurate
        ModSavedData.getSaveData().validateBeeNestEntries();
        ModSavedData.getSaveData().validateNoRaidZoneEntries();
        ModSavedData.getSaveData().validateAreasOfInterest();
        MobProfileUtil.cleanUpInvalidMobProfiles();
        WardZoneUtil.updateAllZones();
    }
}
