package com.github.sculkhorde.systems.gravemind_system.entity_factory;

import com.github.sculkhorde.common.entity.*;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.gravemind_system.Gravemind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
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

    public static void initialize()
    {
        //Add entries to the entity factory
        EntityFactoryEntry[] entries = {
                new EntityFactoryEntry(ModEntities.SCULK_SPORE_SPEWER.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Immature)
                        .setCost((int) SculkSporeSpewerEntity.MAX_HEALTH)
                        .setLimit(1)
                        .setExplicitlyDeniedSenders(ReinforcementRequest.senderType.SculkMass)
                        .addStrategicValues(EntityFactoryEntry.StrategicValues.Infector, EntityFactoryEntry.StrategicValues.EffectiveOnGround)
                        .setMinimumDifficulty(Difficulty.EASY),

                new EntityFactoryEntry(ModEntities.SCULK_PHANTOM.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Mature)
                        .setCost((int) SculkPhantomEntity.MAX_HEALTH)
                        .setLimit(1)
                        .setChanceToSpawn(0.1F)
                        .setExplicitlyDeniedSenders(ReinforcementRequest.senderType.SculkMass, ReinforcementRequest.senderType.Raid)
                        .addStrategicValues(
                        EntityFactoryEntry.StrategicValues.Infector,
                        EntityFactoryEntry.StrategicValues.Melee,
                        EntityFactoryEntry.StrategicValues.EffectiveOnGround,
                        EntityFactoryEntry.StrategicValues.EffectiveInSkies)
                        .setMinimumDifficulty(Difficulty.EASY),

                new EntityFactoryEntry(ModEntities.SCULK_RAVAGER.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Immature)
                        .setCost((int) SculkRavagerEntity.MAX_HEALTH)
                        .setLimit(1)
                        .setExplicitlyDeniedSenders(ReinforcementRequest.senderType.SculkMass)
                        .addStrategicValues(
                        EntityFactoryEntry.StrategicValues.Combat,
                        EntityFactoryEntry.StrategicValues.Tank,
                        EntityFactoryEntry.StrategicValues.Melee,
                        EntityFactoryEntry.StrategicValues.EffectiveOnGround)
                        .setMinimumDifficulty(Difficulty.EASY),

                new EntityFactoryEntry(ModEntities.SCULK_HATCHER.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Undeveloped)
                        .setCost((int) SculkHatcherEntity.MAX_HEALTH)
                        .setExplicitlyDeniedSenders(ReinforcementRequest.senderType.SculkMass)
                        .addStrategicValues(
                        EntityFactoryEntry.StrategicValues.Combat,
                        EntityFactoryEntry.StrategicValues.Melee,
                        EntityFactoryEntry.StrategicValues.EffectiveOnGround)
                        .setMinimumDifficulty(Difficulty.EASY),

                new EntityFactoryEntry(ModEntities.SCULK_CREEPER.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Immature)
                        .setCost(20)
                        .addStrategicValues(
                        EntityFactoryEntry.StrategicValues.Combat,
                        EntityFactoryEntry.StrategicValues.Melee,
                        EntityFactoryEntry.StrategicValues.EffectiveOnGround)
                        .setMinimumDifficulty(Difficulty.NORMAL),

                new EntityFactoryEntry(ModEntities.SCULK_SPITTER.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Undeveloped)
                        .setCost(20)
                        .addStrategicValues(
                        EntityFactoryEntry.StrategicValues.Combat,
                        EntityFactoryEntry.StrategicValues.Ranged,
                        EntityFactoryEntry.StrategicValues.EffectiveOnGround)
                        .setMinimumDifficulty(Difficulty.EASY),

                new EntityFactoryEntry(ModEntities.SCULK_ZOMBIE.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Undeveloped)
                        .setCost(20)
                        .addStrategicValues(
                        EntityFactoryEntry.StrategicValues.Combat,
                        EntityFactoryEntry.StrategicValues.Melee,
                        EntityFactoryEntry.StrategicValues.EffectiveOnGround)
                        .setMinimumDifficulty(Difficulty.EASY),

                new EntityFactoryEntry(ModEntities.SCULK_VINDICATOR.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Immature)
                        .setCost((int) SculkVindicatorEntity.MAX_HEALTH)
                        .addStrategicValues(
                        EntityFactoryEntry.StrategicValues.Combat,
                        EntityFactoryEntry.StrategicValues.Melee,
                        EntityFactoryEntry.StrategicValues.EffectiveOnGround)
                        .setMinimumDifficulty(Difficulty.EASY),

                new EntityFactoryEntry(ModEntities.SCULK_MITE_AGGRESSOR.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Undeveloped)
                        .setCost(6)
                        .addStrategicValues(
                        EntityFactoryEntry.StrategicValues.Combat,
                        EntityFactoryEntry.StrategicValues.Melee,
                        EntityFactoryEntry.StrategicValues.EffectiveOnGround)
                        .setMinimumDifficulty(Difficulty.EASY),

                new EntityFactoryEntry(ModEntities.SCULK_MITE.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Undeveloped)
                        .setCost((int) SculkMiteEntity.MAX_HEALTH)
                        .addStrategicValues(
                        EntityFactoryEntry.StrategicValues.Infector,
                        EntityFactoryEntry.StrategicValues.Melee,
                        EntityFactoryEntry.StrategicValues.EffectiveOnGround)
                        .setMinimumDifficulty(Difficulty.EASY),

                new EntityFactoryEntry(ModEntities.SCULK_SALMON.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Undeveloped)
                        .setCost((int) SculkSalmonEntity.MAX_HEALTH)
                        .setExplicitlyDeniedSenders(ReinforcementRequest.senderType.Raid)
                        .addStrategicValues(
                                EntityFactoryEntry.StrategicValues.Melee,
                                EntityFactoryEntry.StrategicValues.Aquatic)
                        .setMinimumDifficulty(Difficulty.EASY),

                new EntityFactoryEntry(ModEntities.SCULK_LEECH.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Undeveloped)
                        .setCost((int) SculkLeechEntity.MAX_HEALTH)
                        .setExplicitlyDeniedSenders(ReinforcementRequest.senderType.Raid)
                        .addStrategicValues(
                                EntityFactoryEntry.StrategicValues.Infector,
                                EntityFactoryEntry.StrategicValues.Melee,
                                EntityFactoryEntry.StrategicValues.Aquatic)
                        .setMinimumDifficulty(Difficulty.EASY),

                new EntityFactoryEntry(ModEntities.SCULK_STINGER.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Undeveloped)
                        .setCost((int) SculkLeechEntity.MAX_HEALTH)
                        .setExplicitlyDeniedSenders(ReinforcementRequest.senderType.Raid)
                        .addStrategicValues(
                                EntityFactoryEntry.StrategicValues.Infector,
                                EntityFactoryEntry.StrategicValues.Melee,
                                EntityFactoryEntry.StrategicValues.EffectiveInSkies)
                        .setMinimumDifficulty(Difficulty.NORMAL),

                new EntityFactoryEntry(ModEntities.SCULK_SQUID.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Immature)
                        .setCost((int) SculkSquidEntity.MAX_HEALTH)
                        .setLimit(1)
                        .setExplicitlyDeniedSenders(ReinforcementRequest.senderType.Raid)
                        .addStrategicValues(
                                EntityFactoryEntry.StrategicValues.Infector,
                                EntityFactoryEntry.StrategicValues.Melee,
                                EntityFactoryEntry.StrategicValues.Aquatic)
                        .setMinimumDifficulty(Difficulty.NORMAL),

                new EntityFactoryEntry(ModEntities.SCULK_GUARDIAN.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Mature)
                        .setCost((int) SculkGuardianEntity.MAX_HEALTH)
                        .setLimit(1)
                        .setExplicitlyDeniedSenders(ReinforcementRequest.senderType.Raid)
                        .addStrategicValues(
                                EntityFactoryEntry.StrategicValues.Combat,
                                EntityFactoryEntry.StrategicValues.Ranged,
                                EntityFactoryEntry.StrategicValues.Aquatic)
                        .setMinimumDifficulty(Difficulty.NORMAL),

                new EntityFactoryEntry(ModEntities.SCULK_PUFFERFISH.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Immature)
                        .setCost((int) SculkPufferfishEntity.MAX_HEALTH)
                        .setExplicitlyDeniedSenders(ReinforcementRequest.senderType.Raid)
                        .addStrategicValues(
                                EntityFactoryEntry.StrategicValues.Support,
                                EntityFactoryEntry.StrategicValues.Combat,
                                EntityFactoryEntry.StrategicValues.Melee,
                                EntityFactoryEntry.StrategicValues.Aquatic)
                        .setMinimumDifficulty(Difficulty.NORMAL),

                new EntityFactoryEntry(ModEntities.SCULK_WITCH.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Mature)
                        .setCost((int) SculkWitchEntity.MAX_HEALTH)
                        .setChanceToSpawn(0.5F)
                        .setLimit(2)
                        .addStrategicValues(
                        EntityFactoryEntry.StrategicValues.Support,
                        EntityFactoryEntry.StrategicValues.Combat,
                        EntityFactoryEntry.StrategicValues.Melee,
                        EntityFactoryEntry.StrategicValues.EffectiveOnGround)
                        .setMinimumDifficulty(Difficulty.NORMAL),

                new EntityFactoryEntry(ModEntities.SCULK_SHEEP.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Immature)
                        .setCost((int) SculkSheepEntity.MAX_HEALTH)
                        .addStrategicValues(
                        EntityFactoryEntry.StrategicValues.Combat,
                        EntityFactoryEntry.StrategicValues.Melee,
                        EntityFactoryEntry.StrategicValues.EffectiveOnGround)
                        .setMinimumDifficulty(Difficulty.NORMAL),
        };

        SculkHorde.entityFactory.addEntriesToFactory(entries);

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

    public static Optional<EntityFactoryEntry> getRandomEntry(EntityFactoryEntry.StrategicValues desiredValue)
    {
        ArrayList<EntityFactoryEntry.StrategicValues> desiredValues = new ArrayList<>();
        desiredValues.add(desiredValue);
        return getRandomEntry(null, desiredValues);
    }

    public static Optional<EntityFactoryEntry> getRandomEntry(ArrayList<EntityFactoryEntry.StrategicValues> desiredValues)
    {
        return getRandomEntry(null, desiredValues);
    }

    public static Optional<EntityFactoryEntry> getRandomEntry(ArrayList<EntityFactoryEntry.StrategicValues> excludedValues, ArrayList<EntityFactoryEntry.StrategicValues> desiredValues)
    {
        Optional<EntityFactoryEntry> output = Optional.empty();

        ArrayList<EntityFactoryEntry> possibleEntries = new ArrayList<>();
        for(EntityFactoryEntry entry : entries)
        {
            // If it includes an excluded value, ignore.
            if(excludedValues != null && !excludedValues.isEmpty() && entry.containsAny(excludedValues))
            {
                continue;

            }
            // if the entry is not appropriate, ignore.
            else if(!entry.isEntryAppropriateMinimalCheck())
            {
                continue;
            }
            // if the entry doesn't contain an included value, ignore.
            else if(desiredValues != null && !desiredValues.isEmpty() && !entry.containsAll(desiredValues))
            {
                continue;
            }

            possibleEntries.add(entry);
        }

        if(!possibleEntries.isEmpty())
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

        if(ModSavedData.getSaveData() == null) { return; }

        if(ModSavedData.getSaveData().getSculkAccumulatedMass() <= 0)
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
                ModSavedData.getSaveData().subtractSculkAccumulatedMass(mob.getCost());
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
        if(ModSavedData.getSaveData() == null) { return; }

        if(ModSavedData.getSaveData().getSculkAccumulatedMass() <= 0)
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


