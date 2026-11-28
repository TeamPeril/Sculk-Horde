package com.github.sculkhorde.util;

import com.github.sculkhorde.client.sound.BroodFlightSoundInstance;
import com.github.sculkhorde.core.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.WeakHashMap;

@OnlyIn(Dist.CLIENT)
public class ClientSoundUtil {

    // Tracks active sound loops per mob instance.
    private static final WeakHashMap<Mob, BroodFlightSoundInstance> BROOD_FLIGHT_LOOPS = new WeakHashMap<>();

    /**
     * Toggles or updates the flight sound for a specific broodling entity.
     * Call this inside your client-side entity tick loop or an animation updater.
     *
     * @param broodling  The mob entity making the sound
     * @param isFlying   Whether the mob is currently in a flight state
     */
    public static void requestBroodFlightSound(Mob broodling, boolean isFlying) {
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
