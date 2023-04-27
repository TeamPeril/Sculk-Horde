package com.github.sculkhorde.core.gravemind;

import com.github.sculkhorde.common.block.BlockInfestation.InfestationConversionHandler;
import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactory;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactoryEntry;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.BlockSearcher;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.function.Predicate;

public class RaidHandler {

    //The world
    private static ServerLevel level;

    private static BlockPos raidLocation = BlockPos.ZERO;

    private static boolean isRaidActive = false;

    private static int raidRadius = 150;

    private static int raidPartySize = 50;

    private static ArrayList<ISculkSmartEntity> raidParticipants;

    private enum RaidState {
        INACTIVE,
        INITIALIZING,
        ACTIVE,
        COMPLETE
    }

    private static RaidState raidState = RaidState.INACTIVE;

    private static BlockSearcher blockSearcher;

    private static Predicate<BlockPos> isObstructed = (blockPos) -> {
            return !level.getBlockState(blockPos).isSolidRender(level, blockPos);
    };

    private static Predicate<BlockPos> isTarget = (blockPos) -> {
        if(level.getBlockState(blockPos.above()).isAir() && BlockAlgorithms.getBlockDistance(blockPos, raidLocation) > (raidRadius * 0.75) )
        {
            return true;
        }
        else
        {
            return false;
        }
    };

    public static void createRaid(ServerLevel level, BlockPos raidLocation, int raidRadius)
    {
        setLevel(level);
        setRaidLocation(raidLocation);
        setRaidRadius(raidRadius);
        setRaidState(RaidState.INITIALIZING);
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

    /**
     * Gets the raid state
     * @return the raid state
     */
    public static boolean isRaidActive() {
        return raidState == RaidState.ACTIVE;
    }

    /**
     * Sets the raid to active
     */
    public static void setRaidActive() {
        isRaidActive = true;
    }

    /**
     * Sets the raid to inactive
     */
    public static void setRaidInactive() {
        isRaidActive = false;
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
     * Gets the number of raid participants
     * @return the number of raid participants
     */
    public static int getRaidPartySize() {
        return raidPartySize;
    }

    /**
     * Checks if all raid participants are alive
     * @return true if all raid participants are alive, false otherwise
     */
    public static boolean areRaidParticipantsDead() {
        return raidParticipants.stream().allMatch((raidParticipant) -> {
            return !((Mob) raidParticipant).isAlive();
        });
    }

    // Events

    public static void raidTick()
    {
        switch (raidState)
        {
            case INACTIVE:
                inactiveRaidTick();
                break;
            case INITIALIZING:
                initializingRaidTick();
                break;
            case ACTIVE:
                activeRaidTick();
                break;
            case COMPLETE:
                completeRaidTick();
                break;
        }
    }

    private static void inactiveRaidTick()
    {

    }

    private static void initializingRaidTick()
    {
        populateRaidParticipants();

        raidParticipants.forEach((raidParticipant) -> {
            raidParticipant.setParticipatingInRaid(true);
        });

        int MAX_SEARCH_DISTANCE = getRaidRadius();

        if(blockSearcher == null)
        {
            blockSearcher = new BlockSearcher(level, getRaidLocation());
            blockSearcher.setMaxDistance(MAX_SEARCH_DISTANCE);
            blockSearcher.setTargetBlockPredicate(isTarget);
            blockSearcher.setObstructionPredicate(isObstructed);
        }

        blockSearcher.tick();

        if(blockSearcher.isFinished && blockSearcher.isSuccessful)
        {
            raidParticipants.forEach((raidParticipant) -> {
                ((Mob)raidParticipant).setPos(blockSearcher.currentPosition.getX(), blockSearcher.currentPosition.getY() + 1, blockSearcher.currentPosition.getZ());
                level.addFreshEntity((Entity) raidParticipant);
            });

            //Spawn Firework
            FireworkRocketEntity firework = new FireworkRocketEntity(level, blockSearcher.currentPosition.getX(), blockSearcher.currentPosition.getY() + 5, blockSearcher.currentPosition.getZ(), new ItemStack(Items.FIREWORK_ROCKET));
            level.addFreshEntity(firework);

            setRaidState(RaidState.ACTIVE);
            blockSearcher = null;
        }
        else if(blockSearcher.isFinished && !blockSearcher.isSuccessful)
        {
            setRaidState(RaidState.INACTIVE);
            blockSearcher = null;
        }


    }

    private static void activeRaidTick()
    {
        if(areRaidParticipantsDead())
        {
            setRaidState(RaidState.COMPLETE);
        }
    }

    private static void completeRaidTick()
    {
        setRaidState(RaidState.INACTIVE);
    }

    private static Predicate<EntityFactoryEntry> isValidRaidParticipant() {
        return (entityFactoryEntry) -> {
            return entityFactoryEntry.getCategory() == EntityFactory.StrategicValues.Melee || entityFactoryEntry.getCategory() == EntityFactory.StrategicValues.Ranged;
        };
    }

    private static void populateRaidParticipants()
    {
        raidParticipants = new ArrayList<>();

        for(int i = 0; i < getRaidPartySize(); i++)
        {
            raidParticipants.add((ISculkSmartEntity) EntityFactory.getRandomEntry(isValidRaidParticipant()).getEntity().create(level));
        }
    }





}
