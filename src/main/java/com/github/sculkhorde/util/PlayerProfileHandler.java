package com.github.sculkhorde.util;

import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.ModSavedData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class PlayerProfileHandler {

    public static boolean arePlayersOfflineAndSpreadingOfflineDisabled()
    {
        return ServerLifecycleHooks.getCurrentServer().getPlayerCount() <= 0 && !ModConfig.SERVER.isHordeActiveWithNoPlayers.get();
    }

    private static Optional<ModSavedData.PlayerProfileEntry> getPlayerProfile(Player player)
    {
        for(ModSavedData.PlayerProfileEntry entry: ModSavedData.getSaveData().getPlayerProfileEntries())
        {
            if(entry.getPlayerUUID().equals(player.getUUID()))
            {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    private static Optional<ModSavedData.PlayerProfileEntry> getPlayerProfile(UUID uuid)
    {
        for(ModSavedData.PlayerProfileEntry entry: ModSavedData.getSaveData().getPlayerProfileEntries())
        {
            if(entry.getPlayerUUID().equals(uuid))
            {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    public static ModSavedData.PlayerProfileEntry getOrCreatePlayerProfile(Player player)
    {
        Optional<ModSavedData.PlayerProfileEntry> profile =  getPlayerProfile(player);

        if(profile.isPresent())
        {
            return profile.get();
        }
        else
        {
            ModSavedData.PlayerProfileEntry newEntry = new ModSavedData.PlayerProfileEntry(player);
            ModSavedData.getSaveData().getPlayerProfileEntries().add(newEntry);
            return newEntry;

        }
    }

    public static ModSavedData.PlayerProfileEntry getOrCreatePlayerProfile(UUID uuid)
    {
        Optional<ModSavedData.PlayerProfileEntry> profile =  getPlayerProfile(uuid);

        if(profile.isPresent())
        {
            return profile.get();
        }
        else
        {
            ModSavedData.PlayerProfileEntry newEntry = new ModSavedData.PlayerProfileEntry(uuid);
            ModSavedData.getSaveData().getPlayerProfileEntries().add(newEntry);
            return newEntry;

        }
    }

    public static ArrayList<ServerPlayer> getVessels()
    {
        ArrayList<ServerPlayer> list = new ArrayList<>();
        for(ModSavedData.PlayerProfileEntry entry: ModSavedData.getSaveData().getPlayerProfileEntries())
        {
            if(entry.isVessel() && entry.getPlayer().isPresent())
            {
                list.add((ServerPlayer) entry.getPlayer().get());
            }
        }
        return list;
    }

    public static boolean isPlayerVessel(Player player)
    {
        Optional<ModSavedData.PlayerProfileEntry> entry = getPlayerProfile(player);

        if(entry.isPresent())
        {
            return entry.get().isVessel();
        }
        return false;
    }

    public static boolean isPlayerActiveVessel(Player player)
    {
        Optional<ModSavedData.PlayerProfileEntry> entry = getPlayerProfile(player);

        if(entry.isPresent())
        {
            return entry.get().isActiveVessel();
        }
        return false;
    }

    public static boolean isTimeForNextAmbientSound(Player player, long currentGameTime)
    {
        return currentGameTime - getTimeOfLastAmbientSound(player) >= getTimeUntilNextAmbientSound(player);
    }

    public static long getTimeOfLastAmbientSound(Player player)
    {
        Optional<ModSavedData.PlayerProfileEntry> entry = getPlayerProfile(player);

        if(entry.isPresent())
        {
            return entry.get().getTimeOfLastAmbientSound();
        }
        return 0;
    }

    public static void setTimeOfLastAmbientSound(Player player, long value)
    {
        Optional<ModSavedData.PlayerProfileEntry> entry = getPlayerProfile(player);

        if(entry.isPresent())
        {
            entry.get().setTimeOfLastAmbientSound(value);
        }
    }

    public static long getTimeUntilNextAmbientSound(Player player)
    {
        Optional<ModSavedData.PlayerProfileEntry> entry = getPlayerProfile(player);

        if(entry.isPresent())
        {
            return entry.get().getTimeUntilNextAmbientSound();
        }
        return 0;
    }

    public static void setTimeUntilNextAmbientSound(Player player, long value)
    {
        Optional<ModSavedData.PlayerProfileEntry> entry = getPlayerProfile(player);

        if(entry.isPresent())
        {
            entry.get().setTimeUntilNextAmbientSound(value);
        }
    }

    public static void updateGhastDeploymentTime(Player player)
    {
        getOrCreatePlayerProfile(player).setTimeofLastGhastDeployment(player.level().getGameTime());
    }

}
