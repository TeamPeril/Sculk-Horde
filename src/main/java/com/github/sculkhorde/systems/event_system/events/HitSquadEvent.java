package com.github.sculkhorde.systems.event_system.events;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.HitSquadSpawnFinder;
import com.github.sculkhorde.systems.event_system.Event;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.ChunkLoading.EntityChunkLoaderHelper;
import com.github.sculkhorde.util.PlayerProfileHandler;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

public class HitSquadEvent extends Event {
    protected final int MAX_DISTANCE_FROM_PLAYER = 150;


    protected UUID target;
    protected SculkSoulReaperEntity reaper;

    protected enum State {
        INITIALIZATION,
        PURSUIT,
        ENGAGING,
        SUCCESS,
        FAILURE
    }

    protected State state;
    protected boolean isEventOver = false;

    protected Optional<HitSquadSpawnFinder> spawnFinder = Optional.empty();

    protected Optional<BlockPos> desiredSpawnPos = Optional.empty();

    public HitSquadEvent(ResourceKey<Level> dimension, UUID target) {
        super(dimension);
        setEventCost(100);
        this.target = target;
        eventID = target.getMostSignificantBits() & Long.MAX_VALUE;
        setState(State.INITIALIZATION);
    }

    public boolean canContinue()
    {
        return !isEventOver;
    }

    @Override
    public void serverTick() {

        if(state == State.INITIALIZATION)
        {
            initializationTick();
        }
        else if(state == State.PURSUIT)
        {
            pursuitTick();
        }
        else if(state == State.ENGAGING)
        {
            engagingTick();
        }
        else if(state == State.SUCCESS)
        {
            successTick();
        }
        else if(state == State.FAILURE)
        {
            failureTick();
        }

    }

    protected void setState(State state)
    {
        this.state = state;
        SculkHorde.LOGGER.info("HitSquadEvent | " + "State: " + state.toString());
    }

    /**
     * Chooses ONLY an X and Y Coordinate for our block searcher to try and reach.
     * This is NOT the actual spawn location, but getting near it is the goal.
     * @return The Block Position of the desired spawn Location.
     */
    protected BlockPos getDesiredSpawnLocation(BlockPos origin)
    {
        Random rng = new Random();
        float randomAngle = rng.nextFloat(0F, 360F);

        // Calculate the new coordinates
        double radians = Math.toRadians(randomAngle);
        int newX = origin.getX() + (int) (MAX_DISTANCE_FROM_PLAYER * Math.cos(radians));
        int newZ = origin.getZ() + (int) (MAX_DISTANCE_FROM_PLAYER * Math.sin(radians));

        // Create the new BlockPos
        BlockPos newPos = new BlockPos(newX, 0, newZ);
        SculkHorde.LOGGER.debug("HitSquadEvent | Desired Spawn Pos: " + newPos.toShortString());
        return newPos;
    }

    public final Predicate<BlockPos> isValidSpawnPos = (blockPos) ->
    {
        if(desiredSpawnPos.isEmpty()) { return false; }

        // If the X and Y cord are less than 50 blocks
        boolean isXCloseToDesiredSpawn = Math.abs(blockPos.getX() - desiredSpawnPos.get().getX()) < 50;
        boolean isZCloseToDesiredSpawn = Math.abs(blockPos.getZ() - desiredSpawnPos.get().getZ()) < 50;
        boolean isPosCloseToDesiredSpawn = isXCloseToDesiredSpawn && isZCloseToDesiredSpawn;

        return isPosCloseToDesiredSpawn;
    };

    public final Predicate<BlockPos> isObstructed = (blockPos) ->
    {
        boolean isBlockAir = getDimension().getBlockState(blockPos).is(Blocks.AIR);
        boolean isBlockNotExposedToAir = !BlockAlgorithms.isExposedToAir(getDimension(), blockPos);

        return isBlockAir || isBlockNotExposedToAir;
    };

    protected void initializationTick()
    {
        if(getPlayerIfOnline().isEmpty())
        {
            return;
        }

        Player player = getPlayerIfOnline().get();
        setEventLocation(player.blockPosition());

        if(spawnFinder.isEmpty())
        {
            desiredSpawnPos = Optional.of(getDesiredSpawnLocation(player.blockPosition()));
            spawnFinder = Optional.of(new HitSquadSpawnFinder((ServerLevel) player.getLevel(), getEventLocation(), desiredSpawnPos.get()));
            //spawnFinder.get().enableDebugMode();
            spawnFinder.get().setTargetBlockPredicate(isValidSpawnPos);
            spawnFinder.get().setObstructionPredicate(isObstructed);
            spawnFinder.get().setMaxDistance(MAX_DISTANCE_FROM_PLAYER);
        }

        // Tick Block Searcher
        spawnFinder.get().tick();

        // If the block searcher is not finished, return.
        if(!spawnFinder.get().isFinished()) { return; }

        if(spawnFinder.get().isPathFound())
        {
            reaper = SculkSoulReaperEntity.spawnWithDifficulty(player.getLevel(), Vec3.atCenterOf(spawnFinder.get().getFoundBlock()), getTargetProfile().getDifficultyOfNextHit());
            reaper.setHitTarget(player);
            setState(State.PURSUIT);
        }
        else
        {
            if(SculkHorde.isDebugMode()) {
                SculkHorde.LOGGER.info("HitSquadEvent | FAILURE, Could not find good spawn pos.");
            }
            setState(State.FAILURE);
        }
    }

    protected void pursuitTick()
    {
        if(getPlayerIfOnline().isEmpty())
        {
            return;
        }
        Player player = getPlayerIfOnline().get();

        if(player.distanceTo(reaper) <= 64)
        {
            setState(State.ENGAGING);
            return;
        }

        if(player.isDeadOrDying())
        {
            setState(State.SUCCESS);
            SculkHorde.LOGGER.info("HitSquadEvent | EVENT SUCCESS: Player " + player.getScoreboardName() + " died.");
            return;
        }

        if(reaper.isDeadOrDying())
        {
            setState(State.FAILURE);
            SculkHorde.LOGGER.info("HitSquadEvent | EVENT FAILURE: Player " + player.getScoreboardName() + " killed the Soul Reaper.");
            return;
        }

        if(player.distanceTo(reaper) > MAX_DISTANCE_FROM_PLAYER + 50)
        {
            setState(State.FAILURE);
            PlayerProfileHandler.getOrCreatePlayerProfile(player).setTimeOfLastHit(0);
            SculkHorde.LOGGER.info("HitSquadEvent | EVENT FAILURE: Player " + player.getScoreboardName() + " moved too far away from Soul Reaper.");
            return;
        }

        EntityChunkLoaderHelper.getEntityChunkLoaderHelper().createChunkLoadRequestSquareForEntityIfAbsent(reaper,3, 3, TickUnits.convertMinutesToTicks(1));

    }

    protected void engagingTick()
    {
        if(getPlayerIfOnline().isEmpty())
        {
            return;
        }
        Player player = getPlayerIfOnline().get();

        if(player.distanceTo(reaper) > 70)
        {
            setState(State.PURSUIT);
            return;
        }

        if(player.isDeadOrDying())
        {
            setState(State.SUCCESS);
            return;
        }

        if(reaper.isDeadOrDying())
        {
            setState(State.FAILURE);
            SculkHorde.LOGGER.info("HitSquadEvent | EVENT FAILURE: Player " + player.getScoreboardName() + " killed the Soul Reaper.");
            return;
        }

        if(player.distanceTo(reaper) > MAX_DISTANCE_FROM_PLAYER + 50)
        {
            setState(State.FAILURE);
            PlayerProfileHandler.getOrCreatePlayerProfile(player).setTimeOfLastHit(0);
            SculkHorde.LOGGER.info("HitSquadEvent | EVENT FAILURE: Player " + player.getScoreboardName() + " moved too far away from Soul Reaper.");
            reaper.discard();
            return;
        }

        EntityChunkLoaderHelper.getEntityChunkLoaderHelper().createChunkLoadRequestSquareForEntityIfAbsent(reaper,3, 3, TickUnits.convertMinutesToTicks(1));

    }

    protected void successTick()
    {
        getTargetProfile().decreaseDifficultyOfNextHit();
        isEventOver = true;
    }

    protected void failureTick()
    {
        getTargetProfile().increaseDifficultyOfNextHit();
        isEventOver = true;
    }

    public ModSavedData.PlayerProfileEntry getTargetProfile()
    {
        return PlayerProfileHandler.getOrCreatePlayerProfile(target);
    }

    public Optional<Player> getPlayerIfOnline()
    {
        return getTargetProfile().getPlayer();
    }
}
