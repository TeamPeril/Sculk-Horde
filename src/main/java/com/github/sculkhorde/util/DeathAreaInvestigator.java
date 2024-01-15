package com.github.sculkhorde.util;

import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.Gravemind;
import com.github.sculkhorde.core.gravemind.RaidHandler;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

public class DeathAreaInvestigator {

    private BlockSearcher blockSearcher;
    private Optional<ModSavedData.DeathAreaEntry> searchEntry;
    private int ticksSinceLastSuccessfulFind = 0;
    private final int tickIntervalsBetweenSuccessfulFinds = TickUnits.convertMinutesToTicks(1);
    private int ticksSinceLastSearch = 0;
    private final int tickIntervalsBetweenSearches = TickUnits.convertMinutesToTicks(1);

    enum State
    {
        IDLE,
        INITIALIZING,
        SEARCHING,
        FINISHED
    }

    State state = State.IDLE;

    public DeathAreaInvestigator()
    {

    }

    public State getState()
    {
        return state;
    }

    public void setState(State state)
    {
        SculkHorde.LOGGER.info("DeathAreaInvestigator | Old State: " + this.state + ". New State: " + state);
        this.state = state;

    }

    public void idleTick()
    {
        if(SculkHorde.gravemind.getEvolutionState() == Gravemind.evolution_states.Undeveloped || !ModConfig.SERVER.sculk_raid_enabled.get())
        {
            return;
        }

        if(ticksSinceLastSuccessfulFind >= tickIntervalsBetweenSuccessfulFinds && ticksSinceLastSearch >= tickIntervalsBetweenSearches && !RaidHandler.raidData.isRaidActive())
        {
            ticksSinceLastSearch = 0;
            SculkHorde.LOGGER.info("It has been enough time since last death area check. Will see if there is a valid death area.");
            if(SculkHorde.savedData != null) {searchEntry = SculkHorde.savedData.getDeathAreaWithHighestDeaths();}

            if(searchEntry.isPresent())
            {
                setState(State.INITIALIZING);
                SculkHorde.LOGGER.info("Got area with highest deaths.");
                return;
            }
            SculkHorde.LOGGER.info("No death area found.");
        }
    }

    public void initializeTick()
    {
        ServerLevel level = searchEntry.get().getDimension();

        if(level == null)
        {
            SculkHorde.LOGGER.debug("DeathAreaInvestigator | Unable to Locate Dimension " + searchEntry.get().getDimension());
            setState(State.FINISHED);
            return;
        }
        SculkHorde.LOGGER.debug("DeathAreaInvestigator | Starting block search " + searchEntry.get().getDimension());
        blockSearcher = new BlockSearcher(level, searchEntry.get().getPosition());
        blockSearcher.setMaxDistance(25);
        blockSearcher.setObstructionPredicate((pos) -> {
            return level.getBlockState(pos).isAir();
        });
        blockSearcher.setTargetBlockPredicate((pos) -> {
            return level.getBlockState(pos).is(ModBlocks.BlockTags.SCULK_RAID_TARGET_HIGH_PRIORITY)
            || level.getBlockState(pos).is(ModBlocks.BlockTags.SCULK_RAID_TARGET_LOW_PRIORITY)
            || level.getBlockState(pos).is(ModBlocks.BlockTags.SCULK_RAID_TARGET_MEDIUM_PRIORITY);
        });
        setState(State.SEARCHING);
    }

    public void searchTick()
    {
        blockSearcher.tick();

        if(blockSearcher.isFinished && blockSearcher.isSuccessful)
        {
            ticksSinceLastSuccessfulFind = 0;
            setState(State.FINISHED);
            //Send message to all players
            SculkHorde.LOGGER.info("DeathAreaInvestigator | Located Important Blocks at " + searchEntry.get().getPosition() + " in dimension " + searchEntry.get().getDimension());
            // Add to Area of Interest Memory
            if(SculkHorde.savedData != null) {SculkHorde.savedData.addAreaOfInterestToMemory(searchEntry.get().getDimension(), searchEntry.get().getPosition());}
        }
        else if(blockSearcher.isFinished && !blockSearcher.isSuccessful)
        {
            setState(State.FINISHED);
            blockSearcher = null;
            SculkHorde.LOGGER.info("DeathAreaInvestigator | Unable to Locate Important Blocks at " + searchEntry.get().getPosition() + " in dimension " + searchEntry.get().getDimension());
        }
    }

    public void finishedTick()
    {
        SculkHorde.LOGGER.info("DeathAreaInvestigator | Finished");
        if(SculkHorde.savedData != null) { SculkHorde.savedData.removeDeathAreaFromMemory(searchEntry.get().getPosition()); }
        ticksSinceLastSearch = 0;
        setState(State.IDLE);
        blockSearcher = null;
        searchEntry = Optional.empty();
    }

    public void tick()
    {
        ticksSinceLastSuccessfulFind++;
        ticksSinceLastSearch++;
        switch(state)
        {
            case IDLE:
                idleTick();
                break;
            case INITIALIZING:
                initializeTick();
                break;
            case SEARCHING:
                searchTick();
                break;
            case FINISHED:
                finishedTick();
                break;
        }
    }
}
