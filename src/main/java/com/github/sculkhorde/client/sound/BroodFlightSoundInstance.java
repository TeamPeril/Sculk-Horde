package com.github.sculkhorde.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;

public class BroodFlightSoundInstance extends AbstractTickableSoundInstance {
    private final Mob mob;

    public BroodFlightSoundInstance(Mob mob, SoundEvent soundEvent) {
        super(soundEvent, SoundSource.HOSTILE, SoundInstance.createUnseededRandom());
        this.mob = mob;
        this.looping = true; // Tells the OpenAL backend to restart the clip seamlessly
        this.delay = 0;      // Zero gap delay between loops
        this.volume = 0.4F;  // Adjust this value so it doesn't overwhelm players
        this.pitch = 1.0F;
    }

    @Override
    public void tick() {
        // Condition to stop the loop automatically
        // Ensure you replace 'isLivingEntityFlying' with your actual flying check method
        if (this.mob.isRemoved() || !this.mob.isAlive() || this.mob.onGround()) {
            this.stop();
            return;
        }

        // Dynamically shift pitch slightly based on the mob's forward velocity
        // This gives the bug wings a more realistic engine/acceleration buzz feel
        double speed = this.mob.getDeltaMovement().horizontalDistance();
        //this.pitch = 0.85F + (float) (speed * 0.4D);

        // Pin the audio position to the mob's exact 3D coordinates
        this.x = (float) this.mob.getX();
        this.y = (float) this.mob.getY();
        this.z = (float) this.mob.getZ();
    }
}