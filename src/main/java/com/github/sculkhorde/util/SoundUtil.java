package com.github.sculkhorde.util;

import com.github.sculkhorde.core.ModSounds;
import com.github.sculkhorde.systems.debugger_system.DebuggerSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.WeakHashMap;

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
