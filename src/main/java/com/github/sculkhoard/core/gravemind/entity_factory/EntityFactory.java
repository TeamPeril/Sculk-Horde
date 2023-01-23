package com.github.sculkhoard.core.gravemind.entity_factory;

import com.github.sculkhoard.core.SculkHoard;
import com.github.sculkhoard.core.gravemind.Gravemind;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Random;

import static com.github.sculkhoard.core.SculkHoard.gravemind;

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

    private final boolean DEBUG_THIS = false;

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
    public void addEntry(EntityType entity, int cost, StrategicValues value, Gravemind.evolution_states minEvolution)
    {
        entries.add(new EntityFactoryEntry(entity, cost ,value, minEvolution));
    }


    //TODO: Fix this dumb method
    /**
     * Given a strategic value, will return all entires that fit the value.
     * @param value The strategic value to use as a whitelist
     * @return An array list of all entities that fit the filter
     */
    @Nullable
    public static ArrayList<EntityType> getAllEntriesOfThisCategory(StrategicValues value)
    {
        if(entries == null || entries.isEmpty())
            return null;

        ArrayList<EntityType> list = new ArrayList<EntityType>();
        for(EntityFactoryEntry entry : entries)
        {
            if(entry.getCategory() == value)
                list.add(entry.getEntity());
        }

        return list;
    }

    /**
     * Will spawn a reinforcement based on the budget given. Prioritizes spawning the highest costing reinforcement.
     * @param world The world to spawn it in.
     * @param spawnPosition The Position
     * @param noCost Whether it will subtract the cost from the Global Sculk Mass Amount
     */
    public void requestReinforcementAny(World world, BlockPos spawnPosition, boolean noCost, ReinforcementRequest context)
    {
        if(DEBUG_THIS) System.out.println("Reinforcement Request Recieved.");
        //Only continue if Sculk Mass > 0, the entries list is not empty, and if we have a budget

        if(SculkHoard.gravemind.getGravemindMemory().getSculkAccumulatedMass() <= 0)
        {
            return;
        }

        gravemind.processReinforcementRequest(context);
        if(!context.isRequestApproved) { return;}

        //If approved types are not specified, then choose a random appropriate mob
        if(context.approvedMobTypes.isEmpty())
        {
            ArrayList<EntityFactoryEntry> lottery = new ArrayList<>();

            //Loop through list, collect all appropriate entries
            for (EntityFactoryEntry entry : entries) {
                //If valid, spawn the entity
                if (entry.isEntryAppropriate(context))
                {
                    lottery.add(entry);
                }
            }
            int randomEntryIndex = rng.nextInt(lottery.size());
            EntityFactoryEntry randomEntry = lottery.get(randomEntryIndex);
            LivingEntity newEntity = (LivingEntity) randomEntry.getEntity().spawn((ServerWorld) world, null, null, spawnPosition, SpawnReason.SPAWNER, true, true);
            if(newEntity != null) context.spawnedEntity = newEntity;

            if (!noCost)
            {
                SculkHoard.gravemind.getGravemindMemory().subtractSculkAccumulatedMass(randomEntry.getCost());
            }

        }
        else //If not, look for a mob that fits the requirements
        {
            ArrayList<EntityFactoryEntry> approvedEntries = new ArrayList<>();
            //Loop through each entry until we find a valid one
            for (EntityFactoryEntry entry : entries) {
                //If valid, spawn the entity
                if (entry.isEntryAppropriate(context)) {
                    approvedEntries.add(entry);
                }
            }

            //Create Random index
            int randomEntryIndex = rng.nextInt(approvedEntries.size());
            //Get random entry
            EntityFactoryEntry randomEntry = approvedEntries.get(randomEntryIndex);
            //Spawn random entry
            LivingEntity newEntity = (LivingEntity) randomEntry.getEntity().spawn((ServerWorld) world, null, null, spawnPosition, SpawnReason.SPAWNER, true, true);
            if(newEntity != null) context.spawnedEntity = newEntity;
            //If cost enabled, subtract cost
            if (!noCost)
            {
                SculkHoard.gravemind.getGravemindMemory().subtractSculkAccumulatedMass(randomEntry.getCost());
            }

        }

        if(DEBUG_THIS) System.out.println("Reinforcement Request did not meet pre-screening requirements. \n" +
                "is Sculk Mass > 0? " + (SculkHoard.gravemind.getGravemindMemory().getSculkAccumulatedMass() > 0) + "\n" +
                "is entries.size() > 0? " + (entries.size() > 0) + "\n" +
                "is context.budget != 0?" + (context.budget != 0));
    }


    /**
     * Will spawn a reinforcement based on the budget given. Prioritizes spawning random costing reinforcement.
     * @param world The world to spawn it in.
     * @param pos The Position
     */
    public void requestReinforcementSculkMass(World world, BlockPos pos, ReinforcementRequest context)
    {
        //Only continue if Sculk Mass > 0, the entries list is not empty, and if we have a budget
        if(!(SculkHoard.gravemind.getGravemindMemory().getSculkAccumulatedMass() <= 0 || entries.size() == 0 || context.budget == 0))
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
                randomEntry.getEntity().spawn((ServerWorld) world, null, null, pos, SpawnReason.SPAWNER, true, true);
            }

        }
    }
}


