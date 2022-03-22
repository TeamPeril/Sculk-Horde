package com.github.sculkhoard.common.entity.entity_factory;

import com.github.sculkhoard.common.entity.gravemind.Gravemind;
import com.github.sculkhoard.core.SculkHoard;
import com.github.sculkhoard.core.SculkWorldData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Random;

import static com.github.sculkhoard.core.SculkHoard.DEBUG_MODE;
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

    private boolean DEBUG_THIS = false;//DEBUG_MODE && true;

    //The List We Store all the entries in
    private static ArrayList<EntityFactoryEntry> entries;

    public enum StrategicValues {Infector, Melee}
    //TODO: Rename StrategicValues to be more descriptive

    @Nullable
    private static SculkWorldData dataHandler;

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
     * Used to get access to the world saved data.
     * @return
     */
    private SculkWorldData getDataHandler()
    {
        if(dataHandler == null)
        {
            if(ServerLifecycleHooks.getCurrentServer() == null)
                return null;

            DimensionSavedDataManager savedData = ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage();
            dataHandler = savedData.computeIfAbsent(SculkWorldData::new, SculkHoard.SAVE_DATA_ID);
        }
        return dataHandler;
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

    /**
     * Just Returns the Amount of mass the sculk hoard has accumulated.
     * @return The Amount of mass the sculk hoard has accumulated.
     */
    public int getSculkAccumulatedMass()
    {
        return getDataHandler().getSculkAccumulatedMass();
    }

    /**
     * Adds to the sculk accumulated mass
     * @param amount The amount you want to add
     */
    public void addSculkAccumulatedMass(int amount)
    {
        boolean DEBUG_THIS = false;
        getDataHandler().addSculkAccumulatedMass(amount);
        if(DEBUG_MODE && DEBUG_THIS) System.out.println("addSculkAccumulatedMass(" + amount + ")");

    }

    /**
     * Subtracts from the Sculk Accumulate Mass
     * @param amount The amount to substract
     */
    public void subtractSculkAccumulatedMass(int amount)
    {
        getDataHandler().subtractSculkAccumulatedMass(amount);
    }

    /**
     * Sets the value of sculk accumulate mass.
     * @param amount The amount to set it to.
     */
    public void setSculkAccumulatedMass(int amount)
    {
        getDataHandler().setSculkAccumulatedMass(amount);
    }

    /**
     * Will spawn a reinforcement based on the budget given. Prioritizes spawning the highest costing reinforcement.
     * @param world The world to spawn it in.
     * @param pos The Position
     * @param noCost Whether it will subtract the cost from the Global Sculk Mass Amount
     */
    public void requestReinforcementAny(World world, BlockPos pos, boolean noCost, ReinforcementContext context)
    {
        if(DEBUG_THIS) System.out.println("Reinforcement Request Recieved.");
        //Only continue if Sculk Mass > 0, the entries list is not empty, and if we have a budget
        if(getSculkAccumulatedMass() > 0 && entries.size() > 0 && context.budget != 0)
        {
            if(DEBUG_THIS) System.out.println("Reinforcement Request Sent to Gravemind.");
            if(gravemind.processReinforcementRequest(context))
            {
                //If approved types are not specified, then choose a random appropriate mob
                if(context.approvedMobTypes.isEmpty())
                {
                    ArrayList<EntityFactoryEntry> lottery = new ArrayList<EntityFactoryEntry>();

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
                    randomEntry.getEntity().spawn((ServerWorld) world, null, null, pos, SpawnReason.SPAWNER, true, true);

                    if (!noCost) subtractSculkAccumulatedMass(randomEntry.getCost());

                }
                else //If not, look for a mob that fits the requirements
                {
                    //Loop through each entry until we find a valid one
                    for (EntityFactoryEntry entry : entries) {
                        //If valid, spawn the entity
                        if (entry.isEntryAppropriate(context)) {
                            entry.getEntity().spawn((ServerWorld) world, null, null, pos, SpawnReason.SPAWNER, true, true);
                            if (!noCost) subtractSculkAccumulatedMass(entry.getCost());
                            break;
                        }
                    }
                }
            }
        }
        else if(DEBUG_THIS) System.out.println("Reinforcement Request did not meet pre-screening requirements. \n" +
                "is Sculk Mass > 0? " + (getSculkAccumulatedMass() > 0) + "\n" +
                "is entries.size() > 0? " + (entries.size() > 0) + "\n" +
                "is context.budget != 0?" + (context.budget != 0));
    }


    /**
     * Will spawn a reinforcement based on the budget given. Prioritizes spawning the highest costing reinforcement.
     * @param world The world to spawn it in.
     * @param pos The Position
     */
    public void requestReinforcementSculkMass(World world, BlockPos pos, ReinforcementContext context)
    {
        //Only continue if Sculk Mass > 0, the entries list is not empty, and if we have a budget
        if(!(getSculkAccumulatedMass() <= 0 || entries.size() == 0 || context.budget == 0))
        {
            //Spawn Random Mob If appropriate

            context.isRequestViewed = true;
            context.isRequestApproved = true;

            ArrayList<EntityFactoryEntry> lottery = new ArrayList<EntityFactoryEntry>();

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
            //Set Remaining Balance
            context.remaining_balance = context.budget - randomEntry.getCost();
            //Spawn Mob
            randomEntry.getEntity().spawn((ServerWorld) world, null, null, pos, SpawnReason.SPAWNER, true, true);
        }
    }
}


