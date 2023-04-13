package com.github.sculkhorde.core.gravemind.entity_factory;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.Gravemind;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static com.github.sculkhorde.core.SculkHorde.gravemind;

/**
 * The Entity Factory is a data structure that serves as a way for the sculk to
 * make a transaction by spawning a mob, at a cost. It will prioritize spawning the highest
 * costing mob. Right now there is no world variable that stores the amount of accumulated
 * mass by the sculk, so we will worry about that later.
 * <br>
 * NOTE: <br>
 * Items are added to this in ModEventSubscriber.java <br>
 * This is initialized in the main class
 */
public class EntityFactory {

    //The List We Store all the entries in
    private static ArrayList<EntityFactoryEntry> entries;

    //Used to stragecically select units
    public enum StrategicValues {Infector, Melee, Ranged}
    //TODO: Rename StrategicValues to be more descriptive

    //Used to Randomly Select Units
    private static Random rng;

    /**
     * Default Constructor
     */
    public EntityFactory()
    {
        entries = new ArrayList<>();
        rng = new Random();
    }

    /**
     * Adds an entry to the entries Array List
     * @param entity The entity to add
     * @param cost The cost of spawning the entity
     */
    public EntityFactoryEntry addEntry(EntityType entity, int cost, StrategicValues value, Gravemind.evolution_states minEvolution)
    {
        EntityFactoryEntry entry = new EntityFactoryEntry(entity, cost, value, minEvolution);
        entries.add(entry);
        return entry;
    }

    /**
     * Will spawn a reinforcement based on the budget given. Prioritizes spawning the highest costing reinforcement.
     * @param world The world to spawn it in.
     * @param spawnPosition The Position
     * @param noCost Whether it will subtract the cost from the Global Sculk Mass Amount
     */
    public void requestReinforcementAny(Level world, BlockPos spawnPosition, boolean noCost, ReinforcementRequest context)
    {
        boolean DEBUG_THIS = false;
        if(DEBUG_THIS) System.out.println("Reinforcement Request Recieved.");
        //Only continue if Sculk Mass > 0, the entries list is not empty, and if we have a budget

        if(SculkHorde.gravemind.getGravemindMemory().getSculkAccumulatedMass() <= 0)
        {
            return;
        }

        gravemind.processReinforcementRequest(context);
        if(!context.isRequestApproved) { return; }


        ArrayList<EntityFactoryEntry> possibleReinforcements = new ArrayList<>();

        //Loop through list, collect all appropriate entries
        for (EntityFactoryEntry entry : entries)
        {
            //If valid, spawn the entity
            if (entry.isEntryAppropriate(context))
            {
                possibleReinforcements.add(entry);
            }
        }

        if(possibleReinforcements.size() == 0)
        {
            return;
        }

        // Create an array of all reinforcements we will spawn using the spawn positions in context.
        EntityFactoryEntry[] mobsToSpawn = new EntityFactoryEntry[context.positions.length];
        // Create a hashmap to store how many of a specific mob we have spawned
        HashMap<EntityType, Integer> mobCount = new HashMap<>();

        // Fill the array with potential reinforcements
        for (int i = 0; i < context.positions.length; i++)
        {
            if(context.positions[i] == null) { continue; } // If the position is null, skip it


            EntityFactoryEntry randomEntry = null; // Create a random entry
            int attemptsToGetEntry = 0; // This is a fail safe to prevent an infinite loop

            while(randomEntry == null && attemptsToGetEntry < 10)
            {
                // Get a random entry, and if mobsToSpawn contains less than the limit of that entry, add it to the array
                int randomEntryIndex = rng.nextInt(possibleReinforcements.size()); // Create Random index
                randomEntry = possibleReinforcements.get(randomEntryIndex);
                if (randomEntry.getLimit() > 0 && randomEntry.getLimit() > mobCount.getOrDefault(randomEntry.getEntity(), 0))
                {
                    mobsToSpawn[i] = randomEntry;
                    mobCount.put(randomEntry.getEntity(), mobCount.getOrDefault(randomEntry.getEntity(), 0) + 1);
                }
                attemptsToGetEntry++;
            }
        }
        // Spawn((ServerWorld) world, null, null, context.positions[i], SpawnReason.SPAWNER, true, true);
        // Spawn every entry in the array
        for (int i = 0; i < mobsToSpawn.length; i++)
        {
            if(mobsToSpawn[i] == null) { continue; }

            EntityFactoryEntry mob = mobsToSpawn[i];
            context.spawnedEntities[i] = (LivingEntity) mob.getEntity().spawn((ServerLevel) world, context.positions[i], MobSpawnType.SPAWNER);
            if (!noCost)
            {
                SculkHorde.gravemind.getGravemindMemory().subtractSculkAccumulatedMass(mob.getCost());
            }
        }
    }


    /**
     * Will spawn a reinforcement based on the budget given. Prioritizes spawning random costing reinforcement.
     * @param world The world to spawn it in.
     * @param pos The Position
     */
    public void requestReinforcementSculkMass(Level world, BlockPos pos, ReinforcementRequest context)
    {
        //Only continue if Sculk Mass > 0, the entries list is not empty, and if we have a budget
        if(!(SculkHorde.gravemind.getGravemindMemory().getSculkAccumulatedMass() <= 0 || entries.size() == 0 || context.budget == 0))
        {
            //Spawn Random Mob If appropriate

            context.isRequestViewed = true;
            context.isRequestApproved = true;

            ArrayList<EntityFactoryEntry> lottery = new ArrayList<>();

            //Loop through list, collect all appropriate entries
            for (EntityFactoryEntry entry : entries) {
                //If valid, spawn the entity
                if (entry.isEntryAppropriate(context))
                {
                    lottery.add(entry);
                }
            }

            if(!lottery.isEmpty())
            {
                int randomEntryIndex = rng.nextInt(lottery.size());
                EntityFactoryEntry randomEntry = lottery.get(randomEntryIndex);
                //Set Remaining Balance
                context.remaining_balance = context.budget - randomEntry.getCost();
                //Spawn Mob
                randomEntry.getEntity().spawn((ServerLevel) world, pos, MobSpawnType.SPAWNER);
            }
        }
    }
}


