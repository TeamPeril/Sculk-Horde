package com.github.sculkhorde.util;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.systems.debugger_system.DebuggerSystem;
import net.minecraft.core.BlockPos;

public class PerimeterInfestationWardZoneUtil {

    public static ModSavedData.PerimeterInfestationWardZoneEntry getOrCreatePerimeterInfestationWardZone(BlockPos parentRelay)
    {
        if(ModSavedData.getSaveData().getPerimeterInfestationWardZoneEntries().containsKey(parentRelay))
        {
            return ModSavedData.getSaveData().getPerimeterInfestationWardZoneEntries().get(parentRelay);
        }

        DebuggerSystem.cursorDebuggerModule.logDebug("Creating infestation ward zone for relay " + parentRelay.toShortString());
        return createZoneEntry(parentRelay);
    }

    public static boolean doesZoneExist(BlockPos parentRelay)
    {
        return ModSavedData.getSaveData().getPerimeterInfestationWardZoneEntries().containsKey(parentRelay);
    }

    public static ModSavedData.PerimeterInfestationWardZoneEntry createZoneEntry(BlockPos parentRelay)
    {
        ModSavedData.PerimeterInfestationWardZoneEntry zone = new ModSavedData.PerimeterInfestationWardZoneEntry(parentRelay);
        return zone;
    }

    public static boolean isPosInAnyWardZone(BlockPos pos)
    {
        for(ModSavedData.PerimeterInfestationWardZoneEntry zone : ModSavedData.getSaveData().getPerimeterInfestationWardZoneEntries().values())
        {
            if(zone.isPosInsideOfZone(pos))
            {
                return true;
            }
        }

        return false;
    }

    public static void updateAllZones()
    {
        for(ModSavedData.PerimeterInfestationWardZoneEntry zone : ModSavedData.getSaveData().getPerimeterInfestationWardZoneEntries().values())
        {
            zone.updateRelayPositions();
        }
    }
}
