package com.github.sculkhorde.core.gravemind.entity_factory;

import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import static com.github.sculkhorde.core.SculkHorde.gravemind;

/**
 * The Entity Provider is a data structure that serves as a way for the sculk to
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

    private static Random rng;

    /**
     * Default Constructor
     */
    public EntityFactory()
    {
        entries = new ArrayList<>();
        rng = new Random();
    }

    private void addEntryToFactory(EntityFactoryEntry entry)
    {
        entries.add(entry);
    }

    public void addEntriesToFactory( EntityFactoryEntry[] entityFactoryEntries)
    {
        for(EntityFactoryEntry entry : entityFactoryEntries)
        {
            addEntryToFactory(entry);
        }
    }


    public static Optional<EntityFactoryEntry> getRandomEntry(Predicate<EntityFactoryEntry> predicate)
    {
        Optional<EntityFactoryEntry> output = Optional.empty();

        ArrayList<EntityFactoryEntry> possibleEntries = new ArrayList<>();
        for(EntityFactoryEntry entry : entries)
        {
            if(predicate.test(entry) && entry.isEntryAppropriateMinimalCheck())
            {
                possibleEntries.add(entry);
            }
        }

        if(possibleEntries.size() > 0)
        {
            output = Optional.of(possibleEntries.get(rng.nextInt(possibleEntries.size())));
        }

        return output;
    }

    public static Optional<EntityFactoryEntry> getEntry(EntityType<Mob> entityType)
    {
        Optional<EntityFactoryEntry> output = Optional.empty();

        for(EntityFactoryEntry entry : entries)
        {
            if(entry.getEntity() == entityType)
            {
                output = Optional.of(entry);
                break;
            }
        }

        return output;
    }

    public static void spawnReinforcementOfThisEntityType(EntityType entityType, Level level, BlockPos pos)
    {
        Optional<EntityFactoryEntry> entry = getEntry(entityType);
        if(entry.isPresent())
        {
            entry.get().getEntity().spawn((ServerLevel) level, pos, MobSpawnType.SPAWNER);
            SculkHorde.statisticsData.incrementTotalUnitsSpawned();
        }
    }

    /**
     * Will spawn a reinforcement based on the budget given. Prioritizes spawning the highest costing reinforcement.
     * @param world The world to spawn it in.
     * @param spawnPosition The Position
     * @param noCost Whether it will subtract the cost from the Global Sculk Mass Amount
     */
    public void createReinforcementRequestFromSummoner(Level world, BlockPos spawnPosition, boolean noCost, ReinforcementRequest context)
    {
        boolean DEBUG_THIS = false;
        if(DEBUG_THIS) System.out.println("Reinforcement Request Recieved.");
        //Only continue if Sculk Mass > 0, the entries list is not empty, and if we have a budget

        if(SculkHorde.savedData == null) { return; }

        if(SculkHorde.savedData.getSculkAccumulatedMass() <= 0)
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

        if(possibleReinforcements.isEmpty())
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
            ((ServerLevel)world).sendParticles(ParticleTypes.SCULK_SOUL, context.positions[i].getX() + 0.5D, context.positions[i].getY() + 1.15D, context.positions[i].getZ() + 0.5D, 2, 0.2D, 0.0D, 0.2D, 0.0D);
            ((ServerLevel)world).playSound((Player)null, context.positions[i], SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0F, 0.6F + 1.0F);
            if (!noCost)
            {
                SculkHorde.savedData.subtractSculkAccumulatedMass(mob.getCost());
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
        if(SculkHorde.savedData == null) { return; }

        if(SculkHorde.savedData.getSculkAccumulatedMass() <= 0)
        {
            return;
        }

        if(entries.size() == 0)
        {
            return;
        }

        if(context.budget == 0)
        {
            return;
        }

        //Spawn Random Mob If appropriate

        gravemind.processReinforcementRequest(context);
        if(!context.isRequestApproved)
        {
            return;
        }

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
            ((ServerLevel)world).sendParticles(ParticleTypes.SCULK_SOUL, pos.getX() + 0.5D, pos.getY() + 1.15D, pos.getZ() + 0.5D, 2, 0.2D, 0.0D, 0.2D, 0.0D);
            ((ServerLevel)world).playSound((Player)null, pos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0F, 0.6F + 1.0F);
        }
    }
}


