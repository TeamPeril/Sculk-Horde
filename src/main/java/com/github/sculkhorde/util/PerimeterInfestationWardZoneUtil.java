package com.github.sculkhorde.util;

import com.github.sculkhorde.common.blockentity.PerimeterInfestationWardRelayBlockEntity;
import com.github.sculkhorde.core.ModSavedData;
import net.minecraft.core.BlockPos;

public class PerimeterInfestationWardZoneUtil {

    public static ModSavedData.PerimeterInfestationWardZoneEntry createZoneEntry(PerimeterInfestationWardRelayBlockEntity blockEntity)
    {
        ModSavedData.PerimeterInfestationWardZoneEntry zone = new ModSavedData.PerimeterInfestationWardZoneEntry();
        zone.parentRelaypos = blockEntity.getBlockPos();
        blockEntity.perimeterInfestationWardZoneUUID = zone.uuid;
        return zone;
    }

    public boolean isPosInAnyWardZone(BlockPos pos)
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
}
