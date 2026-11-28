package com.github.sculkhorde.systems;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.systems.debugger_system.DebuggerSystem;
import com.github.sculkhorde.util.TickUnits;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class BeeNestActivitySystem {

    protected int index = 0;

    protected final long DELAY_BETWEEN_NEST_TOGGLING = TickUnits.convertMinutesToTicks(5);
    protected long timeOfLastToggle = 0;
    protected final int MAX_ENABLED_HIVES = 20;

    final int STATE_IDLE = 0;
    final int STATE_DEACTIVATION = 1;
    final int STATE_ACTIVATION = 2;
    protected int state = STATE_IDLE;
    protected int preprocessIndex = 0;


    public void setStateIdle()
    {
        state = STATE_IDLE;
        DebuggerSystem.eventDebuggerModule.logInfo("BeeNestActivitySystem | State: IDLE");
    }
    public void setStateActivation()
    {
        state = STATE_ACTIVATION;
        DebuggerSystem.eventDebuggerModule.logInfo("BeeNestActivitySystem | State: ACTIVATION");
    }
    public void setStateDeactivation()
    {
        state = STATE_DEACTIVATION;
        timeOfLastToggle = ServerLifecycleHooks.getCurrentServer().overworld().getGameTime();
        DebuggerSystem.eventDebuggerModule.logInfo("BeeNestActivitySystem | State: DEACTIVATION");
    }

    public void idleTick()
    {
        if(TickUnits.hasTicksPassed(timeOfLastToggle, ServerLifecycleHooks.getCurrentServer().overworld(), TickUnits.convertMinutesToTicks(15)))
        {
            setStateDeactivation();
        }
    }

    public void deactivateTick()
    {
        List<ModSavedData.BeeNestEntry> beeNests = ModSavedData.getSaveData().getBeeNestEntriesAsList();

        if(preprocessIndex >= beeNests.size())
        {
            setStateActivation();
            preprocessIndex = 0;
            return;
        }

        ModSavedData.BeeNestEntry currentNest = beeNests.get(preprocessIndex);

        if(currentNest != null && currentNest.isEntryValid() && !currentNest.isDisabled())
        {
            currentNest.disable();
            DebuggerSystem.eventDebuggerModule.logInfo("BeeNestActivitySystem | Disabling Hive at " + currentNest.getPosition().toShortString());
        }
        preprocessIndex++;
    }

    public void activationTick()
    {
        Collection<UUID> nestsToActivate = ModSavedData.getSaveData().getBeeNestsWithLongestInactivity(MAX_ENABLED_HIVES);

        // If We have <= MAX_ENABLED_HIVES amount of hives, then just enable them all.
        if(ModSavedData.getSaveData().getBeeNestEntriesMap().size() <= MAX_ENABLED_HIVES)
        {
            enableAllHives();
            setStateIdle();
            return;
        }

        for(UUID uuid : nestsToActivate)
        {
            ModSavedData.BeeNestEntry entry = ModSavedData.getSaveData().getBeeNestEntry(uuid);
            if(entry != null && entry.isEntryValid() && entry.isDisabled())
            {
                entry.enable();
                DebuggerSystem.eventDebuggerModule.logInfo("BeeNestActivitySystem | Enabling Hive at " + entry.getPosition().toShortString());
            }
        }
    }

    public void serverTick()
    {
        if(ModSavedData.getSaveData() == null) { return; }

        if(state == STATE_IDLE)
        {
            idleTick();
        }
        else if(state == STATE_ACTIVATION)
        {
            activationTick();
        }
        else if(state == STATE_DEACTIVATION)
        {
            deactivateTick();
        }

    }

    protected void enableAllHives()
    {
        DebuggerSystem.eventDebuggerModule.logInfo("BeeNestActivitySystem | Enabling All Hives");

        for(ModSavedData.BeeNestEntry entry : ModSavedData.getSaveData().getBeeNestEntriesAsList())
        {
            entry.enable();
        }
    }
}
