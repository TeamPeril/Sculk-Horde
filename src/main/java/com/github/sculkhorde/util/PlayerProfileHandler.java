package com.github.sculkhorde.util;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Optional;

public class PlayerProfileHandler {

    private static Optional<ModSavedData.PlayerProfileEntry> getPlayerProfile(Player player)
    {
        for(ModSavedData.PlayerProfileEntry entry: SculkHorde.savedData.getPlayerProfileEntries())
        {
            if(entry.getPlayerUUID().equals(player.getUUID()))
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
            SculkHorde.savedData.getPlayerProfileEntries().add(newEntry);
            return newEntry;

        }
    }

    public static ArrayList<ServerPlayer> getVessels()
    {
        ArrayList<ServerPlayer> list = new ArrayList<>();
        for(ModSavedData.PlayerProfileEntry entry: SculkHorde.savedData.getPlayerProfileEntries())
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

}
