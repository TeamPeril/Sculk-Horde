package com.github.sculkhorde.util;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

public class SoundUtil {

    public static void playAmbientSoundInLevel(Level level, BlockPos blockPos, SoundEvent sound)
    {
        level.playSound((Player) null,blockPos, sound, SoundSource.AMBIENT, 1.0F, 1.0F);
    }
    public static void playSoundInLevel(Level level, BlockPos blockPos, SoundEvent sound, SoundSource soundSource)
    {
        level.playSound((Player) null,blockPos, sound, soundSource, 1.0F, 1.0F);
    }

    public static void playHostileSoundInLevel(Level level, BlockPos blockPos, SoundEvent sound)
    {
        level.playSound((Player) null,blockPos, sound, SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    public static void playSoundForEveryPlayer(Level level, SoundEvent soundEvent)
    {
        ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().forEach(player -> playHostileSoundInLevel(level, player.blockPosition(), soundEvent));
    }
}
