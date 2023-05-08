package com.github.sculkhorde.core.gravemind;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.SculkSporeSpewerEntity;
import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactory;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactoryEntry;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.BlockSearcher;
import com.github.sculkhorde.util.ChunkLoaderHelper;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Predicate;

import static com.github.sculkhorde.core.SculkHorde.gravemind;

public class RaidHandler {

    // Raid Variables
    private static ServerLevel level;
    private static BlockPos raidLocation = BlockPos.ZERO;
    private static int raidRadius = 150;
    private static int MAX_RAID_RADIUS = 300;
    private static int MIN_RAID_RADIUS = 50;
    public static int TICKS_BETWEEN_RAIDS = TickUnits.convertHoursToTicks(2);
    private static ArrayList<ISculkSmartEntity> raidParticipants;
    private enum RaidState {
        INACTIVE,
        INITIALIZING_RAID,
        INITIALIZING_WAVE,
        ACTIVE_WAVE,
        COMPLETE
    }
    private static RaidState raidState = RaidState.INACTIVE;

    // Waves
    private static EntityFactory.StrategicValues[] currentWavePattern;
    private static int MAX_WAVES = 5;
    private static int currentWave = 0;
    private static int remainingWaveParticipants = 0;

    // Targets
    private static ArrayList<BlockPos> high_priority_targets;
    private static ArrayList<BlockPos> medium_priority_targets;
    private static ArrayList<BlockPos> low_priority_targets;

    // Block Searcher
    private static BlockSearcher blockSearcher;
    private static Predicate<BlockPos> isObstructed = (blockPos) ->
    {
        // If block isnt solid, its obstructed
        if(!level.getBlockState(blockPos).isSolidRender(level, blockPos))
        {
            return true;
        }

        // If block above is not
        if(!level.getBlockState(blockPos.above()).canBeReplaced() || level.getBlockState(blockPos.above()).is(Blocks.WATER) || level.getBlockState(blockPos.above()).is(Blocks.LAVA))
        {
            return true;
        }

        if(!level.getBlockState(blockPos.above()).canBeReplaced() || level.getBlockState(blockPos.above(1)).is(Blocks.WATER) || level.getBlockState(blockPos.above(1)).is(Blocks.LAVA))
        {
            return true;
        }

        if(!level.getBlockState(blockPos.above()).canBeReplaced() || level.getBlockState(blockPos.above(2)).is(Blocks.WATER) || level.getBlockState(blockPos.above(2)).is(Blocks.LAVA))
        {
            return true;
        }

        return false;
    };
    private static Predicate<BlockPos> isTarget = (blockPos) -> {
        if( BlockAlgorithms.getBlockDistance(blockPos, raidLocation) > (raidRadius * 0.75) )
        {
            return true;
        }

        return false;

    };

    public static void createRaid(ServerLevel level, BlockPos raidLocation, int raidRadius)
    {
        setLevel(level);
        setRaidLocation(raidLocation);
        setRaidRadius(raidRadius);
        setRaidState(RaidState.INITIALIZING_RAID);
    }

    public static void createWave()
    {
        currentWavePattern = getWavePattern();
    }

    // Accessors & Modifiers

    public static ServerLevel getLevel() {
        return level;
    }

    public static void setLevel(ServerLevel levelIn) {
        level = levelIn;
    }

    public static BlockPos getRaidLocation() {
        return raidLocation;
    }

    public static Vec3 getRaidLocationVec3() {
        return new Vec3(raidLocation.getX(), raidLocation.getY(), raidLocation.getZ());
    }

    public static void setRaidLocation(BlockPos raidLocationIn) {
        raidLocation = raidLocationIn;
    }

    public static boolean canRaidStart()
    {
        if(gravemind.getEvolutionState() == Gravemind.evolution_states.Undeveloped)
        {
            return false;
        }

        if(RaidHandler.raidState != RaidState.INACTIVE)
        {
            return false;
        }

        if(!SculkHorde.savedData.isRaidCooldownOver())
        {
            return false;
        }

        return true;
    }

    /**
     * Gets the raid state
     * @return the raid state
     */
    public static boolean isRaidActive() {
        return raidState == RaidState.ACTIVE_WAVE;
    }

    /**
     * Sets the raid State
     * @param raidStateIn the raid state
     */
    public static void setRaidState(RaidState raidStateIn) {
        raidState = raidStateIn;
    }

    /**
     * Gets the raid radius
     * @return the raid radius
     */
    public static int getRaidRadius() {
        return raidRadius;
    }

    /**
     * Sets the raid radius
     * @param raidRadiusIn the raid radius
     */
    public static void setRaidRadius(int raidRadiusIn) {
        raidRadius = raidRadiusIn;
    }

    /**
     * Checks if all raid participants are alive
     * @return true if all raid participants are alive, false otherwise
     */
    public static boolean areRaidParticipantsDead() {
        return remainingWaveParticipants <= 0;
    }

    private static void updateRemainingWaveParticipantsAmount()
    {
        remainingWaveParticipants = 0;
        for(ISculkSmartEntity entity : raidParticipants)
        {
            if(((Mob) entity).isAlive())
            {
                remainingWaveParticipants++;
            }
        }
    }

    // Events

    public static void raidTick()
    {
        switch (raidState)
        {
            case INACTIVE:
                inactiveRaidTick();
                break;
            case INITIALIZING_RAID:
                initializingRaidTick();
                break;
            case INITIALIZING_WAVE:
                initializingWaveTick();
                break;
            case ACTIVE_WAVE:
                activeWaveTick();
                break;
            case COMPLETE:
                completeRaidTick();
                break;
        }
    }

    private static void inactiveRaidTick()
    {
        SculkHorde.savedData.incrementTicksSinceLastRaid();
    }

    private static void initializingRaidTick()
    {
        SculkHorde.savedData.setTicksSinceLastRaid(0);
        int MAX_SEARCH_DISTANCE = getRaidRadius();

        if(blockSearcher == null)
        {
            //Send message to all players
            level.players().forEach((player) -> {
                player.displayClientMessage(Component.literal("Initializing Raid at: " + RaidHandler.getRaidLocation()), false);
            });

            blockSearcher = new BlockSearcher(level, getRaidLocation());
            blockSearcher.setMaxDistance(MAX_SEARCH_DISTANCE);
            blockSearcher.setTargetBlockPredicate(isTarget);
            blockSearcher.setObstructionPredicate(isObstructed);

            ChunkLoaderHelper.forceLoadChunksInRadius(level, getRaidLocation(), getRaidLocation().getX() >> 4, getRaidLocation().getZ() >> 4, 5);

        }

        blockSearcher.tick();

        if(blockSearcher.isFinished && blockSearcher.isSuccessful)
        {
            setRaidState(RaidState.INITIALIZING_WAVE);

            // Summon Sculk Spore Spewer at location
            SculkSporeSpewerEntity sporeSpewer = new SculkSporeSpewerEntity(EntityRegistry.SCULK_SPORE_SPEWER.get(), level);
            sporeSpewer.setPos(blockSearcher.currentPosition.getX(), blockSearcher.currentPosition.getY(), blockSearcher.currentPosition.getZ());
            level.addFreshEntity(sporeSpewer);
        }
        else if(blockSearcher.isFinished && !blockSearcher.isSuccessful)
        {
            setRaidState(RaidState.INACTIVE);
            blockSearcher = null;


            SculkHorde.LOGGER.debug("Raid Failed to Initialize");
        }


    }

    private static void initializingWaveTick()
    {

        createWave();
        populateRaidParticipants();

        level.players().forEach((player) ->
        {
            // If player is close
            if(player.distanceToSqr(blockSearcher.currentPosition.getX(), blockSearcher.currentPosition.getY(), blockSearcher.currentPosition.getZ()) < raidRadius*2)
            {
                player.displayClientMessage(Component.literal("Starting Wave " + currentWave + "."), false);
            }
        });

        raidParticipants.forEach((raidParticipant) ->
        {
            raidParticipant.setParticipatingInRaid(true);
            ((Mob)raidParticipant).setPos(blockSearcher.currentPosition.getX(), blockSearcher.currentPosition.getY() + 1, blockSearcher.currentPosition.getZ());
            level.addFreshEntity((Entity) raidParticipant);
            ((Mob) raidParticipant).addEffect(new MobEffectInstance(MobEffects.GLOWING, TickUnits.convertHoursToTicks(1), 0));
        });


        SculkHorde.LOGGER.debug("Spawning mobs at: " + blockSearcher.currentPosition);

        setRaidState(RaidState.ACTIVE_WAVE);
    }

    private static void activeWaveTick()
    {
        updateRemainingWaveParticipantsAmount();
        if(!areRaidParticipantsDead())
        {
            return;
        }

        if(currentWave == MAX_WAVES)
        {
            setRaidState(RaidState.COMPLETE);
            //Send message to all players
            level.players().forEach((player) -> {
                // If player is close
                if(player.distanceToSqr(blockSearcher.currentPosition.getX(), blockSearcher.currentPosition.getY(), blockSearcher.currentPosition.getZ()) < raidRadius*2)
                {
                    player.displayClientMessage(Component.literal("Completed Final Wave."), false);
                }
            });
            return;
        }
        currentWave++;
        //Send message to all players
        level.players().forEach((player) -> {
            // If player is close
            if(player.distanceToSqr(blockSearcher.currentPosition.getX(), blockSearcher.currentPosition.getY(), blockSearcher.currentPosition.getZ()) < raidRadius*2)
            {
                player.displayClientMessage(Component.literal("Wave " + (currentWave) + " complete."), false);
            }
        });

        setRaidState(RaidState.INITIALIZING_WAVE);

    }

    private static void completeRaidTick()
    {
        blockSearcher = null; // Reset blockSearcher since were done using it to spawn mobs
        currentWave = 0;
        ChunkLoaderHelper.unloadChunksInRadius(level, getRaidLocation(), getRaidLocation().getX() >> 4, getRaidLocation().getZ() >> 4, 5);
        setRaidState(RaidState.INACTIVE);
    }

    private static Predicate<EntityFactoryEntry> isValidRaidParticipant(EntityFactory.StrategicValues strategicValue)
    {
        return (entityFactoryEntry) -> {
            return entityFactoryEntry.getCategory() == strategicValue;
        };
    }

    public static EntityFactory.StrategicValues[] getWavePattern()
    {
        EntityFactory.StrategicValues[][] possibleWavePatterns = {DefaultRaidWavePatterns.FIVE_RANGED_FIVE_MELEE, DefaultRaidWavePatterns.TEN_RANGED, DefaultRaidWavePatterns.TEN_MELEE};
        Random random = new Random();
        return possibleWavePatterns[random.nextInt(possibleWavePatterns.length)];
    }

    private static void populateRaidParticipants()
    {
        raidParticipants = new ArrayList<>();

        for(int i = 0; i < getWavePattern().length; i++)
        {
            EntityFactoryEntry randomEntry = EntityFactory.getRandomEntry(isValidRaidParticipant(getWavePattern()[i]));
            raidParticipants.add((ISculkSmartEntity) randomEntry.getEntity().create(level));
        }
    }
}
