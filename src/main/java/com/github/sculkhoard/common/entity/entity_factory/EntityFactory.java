package com.github.sculkhoard.common.entity.entity_factory;
import com.github.sculkhoard.core.EntityRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;

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

    /**
     * Default Constructor
     */
    public EntityFactory()
    {
        entries = new ArrayList<>();
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
     * Will spawn a reinforcement based on the deposit given. This processes consumes
     * sculk mass from the deposit. Prioritizes spawning the highest costing reinforcement.
     * @param deposit The variable the cost will be taken from.
     * @param world The world to spawn it in.
     * @param pos The Position
     * @return The Remaining Balance
     */
    public int requestReinforcementAny(int deposit, World world, BlockPos pos)
    {
        //Go through each entry starting from beginning until it is something we can afford
        for(int i = 0; i < entries.size(); i++)
        {
            EntityFactoryEntry currentEntry = entries.get(i);
            if(currentEntry.getCost() <= deposit)
            {
                currentEntry.getEntity().spawn((ServerWorld) world, null, null, pos, SpawnReason.SPAWNER, true, true);
                return deposit - currentEntry.getCost();
            }
        }
        return deposit;
    }
}


