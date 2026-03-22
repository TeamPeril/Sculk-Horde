package com.github.sculkhorde.systems;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.debugger_system.DebuggerSystem;
import com.github.sculkhorde.util.TickUnits;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;

public class BeeNestActivitySystem {

    protected int index = 0;

    protected final long DELAY_BETWEEN_TICKS = TickUnits.convertSecondsToTicks(0.25F);
    protected long timeOfLastTick = 0;
    protected boolean isActive = false;
    protected boolean startEnablingHives = false;
    protected int enabledHives = 0;
    protected final int MAX_ENABLED_HIVES = 20;


    public void activate()
    {
        DebuggerSystem.eventDebuggerModule.logInfo("BeeNestActivitySystem | Activating");
        isActive = true;
    }
    public void deactivate()
    {
        DebuggerSystem.eventDebuggerModule.logInfo("BeeNestActivitySystem | Deactivating");
        isActive = false;
        enabledHives = 0;
        startEnablingHives = false;
    }

    public boolean isActive()
    {
        return isActive;
    }

    public void serverTick()
    {
        if(!isActive() || ModSavedData.getSaveData() == null) { return; }

        // Cooldown Check
        if(Math.abs(ServerLifecycleHooks.getCurrentServer().overworld().getGameTime() - timeOfLastTick) < DELAY_BETWEEN_TICKS)
        {
            return;
        }


        List<ModSavedData.BeeNestEntry> beeNestsList = ModSavedData.getSaveData().getBeeNestEntries();
        if (beeNestsList.isEmpty()) {
            return;
        }

        // If We have <= MAX_ENABLED_HIVES amount of hives, then just enable them all.
        if(beeNestsList.size() <= MAX_ENABLED_HIVES)
        {
            enableAllHives();
            deactivate();
            return;
        }

        // If we have more hives than MAX_ENABLED_HIVES
        // 1. Iterate through list until we find enabled nest.
        // 2. Once we do, disable any hives that are enabled.
        // 3. Enable any disabled hives until we reach MAX_ENABLED_HIVES limit.

        index += 1;

        if(index >= beeNestsList.size())
        {
            index = 0;
        }

        ModSavedData.BeeNestEntry entry = beeNestsList.get(index);

        if (!entry.isEntryValid()) {
            return;
        }

        // If we find an enabled nest, disable it and start enabling MAX_ENABLED_HIVES amount of hives.
        if (!entry.isOccupantsExistingDisabled()) {
            startEnablingHives = true;
            entry.disableOccupantsExiting();
            DebuggerSystem.eventDebuggerModule.logInfo("BeeNestActivitySystem | Disabling Hive at " + entry.getPosition().toShortString());
        }
        // If we are enabling nests, and we have found an inactive nest, enable it
        else if(startEnablingHives && entry.isOccupantsExistingDisabled() && enabledHives < MAX_ENABLED_HIVES)
        {
            entry.enableOccupantsExiting();
            enabledHives += 1;
            DebuggerSystem.eventDebuggerModule.logInfo("BeeNestActivitySystem | Enabling Hive at " + entry.getPosition().toShortString());
        }

        // If we've enabled all the ones we need to, then deactivate.
        if(enabledHives >= MAX_ENABLED_HIVES && index == 0)
        {
            deactivate();
        }

        // If we've reached the end and have no enabled hives, then enable first hive in list.
        if(enabledHives <= 0 && index == beeNestsList.size() - 1)
        {
            DebuggerSystem.eventDebuggerModule.logInfo("BeeNestActivitySystem | Reached End and found no enabled hives.");
            beeNestsList.get(0).enableOccupantsExiting();
            index = 1;
            enabledHives = 1;
            startEnablingHives = true;
        }
    }

    protected void enableAllHives()
    {
        DebuggerSystem.eventDebuggerModule.logInfo("BeeNestActivitySystem | Enabling All Hives");

        for(ModSavedData.BeeNestEntry entry : ModSavedData.getSaveData().getBeeNestEntries())
        {
            entry.enableOccupantsExiting();
        }
    }
}
