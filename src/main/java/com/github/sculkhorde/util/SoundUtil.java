package com.github.sculkhorde.util;

import com.github.sculkhorde.client.sound.BroodFlightSoundInstance;
import com.github.sculkhorde.core.ModSounds;
import com.github.sculkhorde.systems.debugger_system.DebuggerSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.WeakHashMap;

public class SoundUtil {

    // Tracks active sound loops per mob instance.
    // WeakHashMap automatically removes the entry if the Mob object is garbage collected.
    private static final WeakHashMap<Mob, BroodFlightSoundInstance> BROOD_FLIGHT_LOOPS = new WeakHashMap<>();

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

    /**
     * Toggles or updates the flight sound for a specific broodling entity.
     * Call this inside your client-side entity tick loop or an animation updater.
     *
     * @param broodling  The mob entity making the sound
     * @param soundEvent The registered SoundEvent for the beetle wings
     * @param isFlying   Whether the mob is currently in a flight state
     */
    public static void requestBroodFlightSound(Mob broodling, boolean isFlying) {
        if(!broodling.level().isClientSide)
        {
            DebuggerSystem.entityDebuggerModule.logError("requestFlightSound | attempted client code on server.");
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        // Safety check to ensure the sound engine is loaded and player is in-world
        if (mc.level == null || broodling == null) {
            return;
        }

        boolean isAlreadyPlaying = BROOD_FLIGHT_LOOPS.containsKey(broodling);

        if (isFlying && !isAlreadyPlaying) {
            // 1. Create a new sound loop instance
            BroodFlightSoundInstance newLoop = new BroodFlightSoundInstance(broodling, ModSounds.SCULK_BROOD_FLY.get());

            // 2. Register it in our local tracker
            BROOD_FLIGHT_LOOPS.put(broodling, newLoop);

            // 3. Submit it to Minecraft's audio backend
            mc.getSoundManager().play(newLoop);

        } else if (!isFlying && isAlreadyPlaying) {
            // If the mob has landed or stopped flying, terminate this specific loop
            stopBroodFlightSound(broodling);
        }
    }

    /**
     * Explicitly forces a loop to stop for a specific entity.
     */
    public static void stopBroodFlightSound(Mob broodling) {
        if(!broodling.level().isClientSide)
        {
            DebuggerSystem.entityDebuggerModule.logError("stopFlightSound | attempted client code on server.");
            return;
        }

        if (BROOD_FLIGHT_LOOPS.containsKey(broodling)) {
            BroodFlightSoundInstance activeInstance = BROOD_FLIGHT_LOOPS.get(broodling);
            if (activeInstance != null) {
                // Tells the vanilla sound manager to stop executing this instance loop
                Minecraft.getInstance().getSoundManager().stop(activeInstance);
            }
            BROOD_FLIGHT_LOOPS.remove(broodling);
        }
    }
}
