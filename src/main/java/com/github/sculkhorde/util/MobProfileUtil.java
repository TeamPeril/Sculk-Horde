package com.github.sculkhorde.util;

import com.github.sculkhorde.core.ModSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.Optional;

public class MobProfileUtil {

    private static Optional<ModSavedData.MobProfileEntry> getMobProfile(Mob mob)
    {
        for(ModSavedData.MobProfileEntry entry: ModSavedData.getSaveData().getMobProfileEntries())
        {
            if(entry.getEntityType().equals(mob.getType()))
            {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }


    public static ModSavedData.MobProfileEntry getOrCreateMobProfile(Mob mob)
    {
        Optional<ModSavedData.MobProfileEntry> profile =  getMobProfile(mob);

        if(profile.isPresent())
        {
            return profile.get();
        }
        else
        {
            ModSavedData.MobProfileEntry newEntry = new ModSavedData.MobProfileEntry(mob);
            ModSavedData.getSaveData().getMobProfileEntries().add(newEntry);
            return newEntry;

        }
    }

    public static boolean canSendGhastDeployment(LivingEntity entity){

        if(entity instanceof Mob mob)
        {
            ModSavedData.MobProfileEntry profile = getOrCreateMobProfile(mob);

            if(entity.level().getGameTime() - profile.getTimeofLastGhastDeployment() < TickUnits.convertMinutesToTicks(1))
            {
                return false;
            }
            else if(!profile.isHighPriorityTarget())
            {
                return false;
            }
        }

        Optional<ModSavedData.NodeEntry> node = ModSavedData.getSaveData().getClosestNodeEntry((ServerLevel) entity.level(), entity.blockPosition());
        if(node.isEmpty())
        {
            return false;
        }

        return true;

    }

    public static void updateGhastDeploymentTime(Mob mob)
    {
        getOrCreateMobProfile(mob).setTimeofLastGhastDeployment(mob.level().getGameTime());
    }
}
