package com.github.sculkhoard.common.entity.entity_factory;
import com.github.sculkhoard.core.SculkHoard;
import com.github.sculkhoard.core.SculkWorldData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;

import static com.github.sculkhoard.core.SculkHoard.DEBUG_MODE;

/**
 * The Entity Factory is a data structure that serves as a way for the sculk to
 * make a transaction by spawning a mob, at a cost. It will prioritize spawning the highest
 * costing mob. Right now there is no world variable that stores the amount of accumulated
 * mass by the sculk, so we will worry about that later.
 * <br>
 * NOTE: Items are added to this in ModEventSubscriber.java
 */
public class EntityFactory {

    //The List We Store all the entries in
    private static ArrayList<EntityFactoryEntry> entries;

    @Nullable
    private static SculkWorldData dataHandler;

    /**
     * Default Constructor
     */
    public EntityFactory()
    {
        entries = new ArrayList<>();
    }

    /**
     * Used to get access to the world saved data.
     * @return
     */
    private SculkWorldData  getDataHandler()
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
    public void addEntry(EntityType entity, int cost)
    {
        entries.add(new EntityFactoryEntry(entity, cost));
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
        boolean DEBUG_THIS = true;
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
     * @param budget The max amount to spend
     * @param world The world to spawn it in.
     * @param pos The Position
     * @param noCost Whether it will subtract the cost from the Global Sculk Mass Amount
     * @return The Remaining Balance
     */
    public int requestReinforcementAny(int budget, World world, BlockPos pos, boolean noCost)
    {
        //If no Sculk Mass, then just return original budget
        if(getSculkAccumulatedMass() <= 0 || entries.size() == 0 || budget == 0) {return budget;}

        //Go through each entry starting from beginning until it is something we can afford
        EntityFactoryEntry currentEntry = entries.get(0);
        int currentEntryCost = 0;

        for(int i = 0; i < entries.size(); i++)
        {
            currentEntry = entries.get(i);
            currentEntryCost = currentEntry.getCost();
            if(currentEntryCost <= budget)
            {
                currentEntry.getEntity().spawn((ServerWorld) world, null, null, pos, SpawnReason.SPAWNER, true, true);
                if(!noCost) subtractSculkAccumulatedMass(currentEntryCost);
                return budget - currentEntry.getCost();
            }
        }

        return budget;
    }

    /**
     * Will spawn a reinforcement based on the budget given.
     * Prioritizes spawning the highest costing reinforcement.
     * Will only spawn mob if on white list.
     * @param budget The max amount to spend
     * @param world The world to spawn it in.
     * @param pos The Position
     * @param list The White List
     * @return The Remaining Balance
     */
    public int requestReinforcementWhiteList(int budget, World world, BlockPos pos, ArrayList<EntityType> list)
    {
        //If no Sculk Mass, then just return original budget
        if(getSculkAccumulatedMass() <= 0) {return budget;}

        //Go through each entry starting from beginning until it is something we can afford
        for(int i = 0; i < entries.size(); i++)
        {
            EntityFactoryEntry currentEntry = entries.get(i);
            if(currentEntry.getCost() <= budget && list.contains(currentEntry))
            {
                currentEntry.getEntity().spawn((ServerWorld) world, null, null, pos, SpawnReason.SPAWNER, true, true);
                return budget - currentEntry.getCost();
            }
        }
        return budget;
    }

    /**
     * Will spawn a reinforcement based on the budget given.
     * Prioritizes spawning the highest costing reinforcement.
     * Won't Spawn Mobs on Black List
     * @param budget The max amount to spend
     * @param world The world to spawn it in.
     * @param pos The Position
     * @param list The Black List
     * @return The Remaining Balance
     */
    public int requestReinforcementBlackList(int budget, World world, BlockPos pos, ArrayList<EntityType> list)
    {
        //If no Sculk Mass, then just return original budget
        if(getSculkAccumulatedMass() <= 0) {return budget;}

        //Go through each entry starting from beginning until it is something we can afford
        for(int i = 0; i < entries.size(); i++)
        {
            EntityFactoryEntry currentEntry = entries.get(i);
            if(currentEntry.getCost() <= budget && !list.contains(currentEntry))
            {
                currentEntry.getEntity().spawn((ServerWorld) world, null, null, pos, SpawnReason.SPAWNER, true, true);
                return budget - currentEntry.getCost();
            }
        }
        return budget;
    }
}


