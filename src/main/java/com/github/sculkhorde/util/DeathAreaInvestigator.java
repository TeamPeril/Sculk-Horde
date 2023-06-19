package com.github.sculkhorde.util;

import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.Gravemind;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

import static com.github.sculkhorde.core.SculkHorde.DEBUG_MODE;

public class DeathAreaInvestigator {

    private BlockSearcher blockSearcher;
    private ServerLevel level;
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

    public DeathAreaInvestigator(ServerLevel level)
    {
        this.level = level;
    }

    public State getState()
    {
        return state;
    }

    public void setState(State state)
    {
        this.state = state;
    }

    public void idleTick()
    {
        if(SculkHorde.gravemind.getEvolutionState() == Gravemind.evolution_states.Undeveloped)
        {
            return;
        }

        if(!DEBUG_MODE)
        {
            return;
        }

        if(ticksSinceLastSuccessfulFind >= tickIntervalsBetweenSuccessfulFinds && ticksSinceLastSearch >= tickIntervalsBetweenSearches && !SculkHorde.raidHandler.isRaidActive())
        {
            ticksSinceLastSearch = 0;
            searchEntry = SculkHorde.savedData.getDeathAreaWithHighestDeaths();
            if(searchEntry.isPresent())
            {
                setState(State.INITIALIZING);
            }
        }
    }

    public void initializeTick()
    {
        blockSearcher = new BlockSearcher(level, searchEntry.get().getPosition());
        blockSearcher.setMaxDistance(25);
        blockSearcher.setObstructionPredicate((pos) -> {
            return level.getBlockState(pos).isAir();
        });
        blockSearcher.setTargetBlockPredicate((pos) -> {
            return level.getBlockState(pos).is(BlockRegistry.Tags.SCULK_RAID_TARGET_HIGH_PRIORITY)
            || level.getBlockState(pos).is(BlockRegistry.Tags.SCULK_RAID_TARGET_LOW_PRIORITY)
            || level.getBlockState(pos).is(BlockRegistry.Tags.SCULK_RAID_TARGET_MEDIUM_PRIORITY);
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
            SculkHorde.LOGGER.debug("DeathAreaInvestigator | Located Important Blocks at " + searchEntry.get().getPosition());
            // Add to Area of Interest Memory
            SculkHorde.savedData.addAreaOfInterestToMemory(searchEntry.get().getPosition());
        }
        else if(blockSearcher.isFinished && !blockSearcher.isSuccessful)
        {
            setState(State.FINISHED);
            blockSearcher = null;
            SculkHorde.LOGGER.debug("DeathAreaInvestigator | Unable to Locate Important Blocks at " + searchEntry.get().getPosition());
        }
    }

    public void finishedTick()
    {
        SculkHorde.savedData.removeDeathAreaFromMemory(searchEntry.get().getPosition());
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
