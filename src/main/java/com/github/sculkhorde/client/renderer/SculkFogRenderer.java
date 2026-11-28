package com.github.sculkhorde.client.renderer;

import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.util.ColorUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FogType;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.joml.Vector3f;

public class SculkFogRenderer {

    @SubscribeEvent
    public void renderFogListener(ViewportEvent.RenderFog event) {
        Entity entity = event.getCamera().getEntity();

        if (entity instanceof Player player) {
            MobEffectInstance effect = player.getEffect(ModMobEffects.SCULK_FOG);

            if (effect == null)
            {
                return;
            }

            float fogDistance = 16.0f;
            float distance = 0;

            int duration = effect.getDuration();
            int blockY = effect.getAmplifier();
            int playerY = player.blockPosition().getY();

            if ((playerY-64) > blockY) {
                distance = (playerY - blockY) - 16;
                distance = Math.max(distance, 16);

                fogDistance += distance - 16;
            }

            //float times = (duration <= 40) ? 1.5f : 1f;
            if (duration <= 160) {fogDistance = fogDistance + (160 - duration);}

            RenderSystem.setShaderFogColor(0.071f, 0.118f, 0.188f);
            RenderSystem.setShaderFogStart(0.0F);
            RenderSystem.setShaderFogEnd(fogDistance);

        }
    }

    /*
    @SubscribeEvent
    public void renderDiseasedAtmosphereListener(ViewportEvent.RenderFog event) {
        Entity entity = event.getCamera().getEntity();

        if (entity instanceof Player player) {
            // 1. Check for the specific MobEffect
            MobEffectInstance effect = player.getEffect(ModMobEffects.DISEASED_ATMOSPHERE);

            if (effect == null) {
                return;
            }

            // 2. Define Fog Parameters
            // Using event.getFarPlaneDistance() as a base is a good practice, but for a dense fog effect
            // like Darkness, you might want to use a fixed, short distance.
            float baseDistance = event.getFarPlaneDistance();
            float fogStart = 5.0f;
            float fogEnd = 16.0f; // This is your fogDistance from the original code

            // Use the colors from your original RenderSystem.setShaderFogColor call
            float r = 0.071f;
            float g = 0.118f;
            float b = 0.188f;

            // 3. Handle Fog Type
            // You MUST check the type to handle Sky Fog separately from Terrain Fog.

            // Handles Fog for Blocks/Terrain/Water (FogType.FOG_TERRAIN)
            if (event.getMode().equals(FogRenderer.FogMode.FOG_TERRAIN)) {
                // Set the new fog color
                RenderSystem.setShaderFogColor(r, g, b);

                // Set the new fog distance/density
                event.setNearPlaneDistance(fogStart);
                event.setFarPlaneDistance(fogEnd);

                // The event must be canceled for the changes to take effect
                event.setCanceled(true);
            }

            // Handles Fog for the Sky/Clouds (FogType.FOG_SKY)
            if (event.getMode().equals(FogRenderer.FogMode.FOG_SKY)) {
                // Set the new fog color
                RenderSystem.setShaderFogColor(r, g, b);

                // For the sky, we often set the far plane distance to be much shorter than vanilla
                // to make the sky blend into the fog quickly.
                event.setFarPlaneDistance(fogEnd * 2.0F); // Extend slightly beyond terrain fog end

                // The event must be canceled for the changes to take effect
                event.setCanceled(true);
            }
        }
    }

     */

    private static boolean SKY_CHUNK_LOADED = false;

    private static float SKY_FAR = 0.0F;
    private static float SKY_NEAR = 0.0F;

    private static boolean TERRAIN_CHUNK_LOADED = false;

    private static float TERRAIN_FAR = 0.0F;
    private static float TERRAIN_NEAR = 0.0F;

    /**
     * Do Fog
     * Reference: <a href="https://github.com/TeamTwilight/twilightforest/blob/1.20.1/src/main/java/twilightforest/client/FogHandler.java">...</a>
     * @param event
     */
    @SubscribeEvent
    public void renderDiseasedAtmosphereListener(ViewportEvent.RenderFog event) {
        if (event.getType().equals(FogType.NONE) && Minecraft.getInstance().cameraEntity instanceof LocalPlayer player && player.level() instanceof ClientLevel clientLevel) {

            MobEffectInstance effect = player.getEffect(ModMobEffects.DISEASED_ATMOSPHERE);

            if (effect == null) {
                return;
            }

            //RenderSystem.setShaderFogColor(0.071f, 0.118f, 0.188f);

            if (event.getMode().equals(FogRenderer.FogMode.FOG_SKY))
            {
                /*
                if (SKY_CHUNK_LOADED) {
                    event.setCanceled(true);

                    float CONSTANT_NEAR = 16.0F; // Fog starts 1 block away
                    float CONSTANT_FAR = 32.0F; // Fog ends 20 blocks away

                    // Apply the set values directly to the event
                    event.setFarPlaneDistance(CONSTANT_FAR);
                    event.setNearPlaneDistance(CONSTANT_NEAR);

                } else if (clientLevel.isLoaded(player.blockPosition())) { //We do a first-time set up after the chunk the player is in is loaded
                    SKY_CHUNK_LOADED = true;
                    SKY_FAR = event.getFarPlaneDistance() * 0.5F;
                    SKY_NEAR = 0.0F;
                }

                 */
            } else {
                if (TERRAIN_CHUNK_LOADED) {
                    event.setCanceled(true);

                    float CONSTANT_NEAR = 16.0F; // Fog starts 1 block away
                    float CONSTANT_FAR = 32.0F; // Fog ends 20 blocks away

                    // Apply the set values directly to the event
                    event.setFarPlaneDistance(CONSTANT_FAR);
                    event.setNearPlaneDistance(CONSTANT_NEAR);

                    Vector3f sculk_fog = ColorUtil.hexToVector3F(ColorUtil.sculkBaseColor1);
                    RenderSystem.setShaderFogColor(sculk_fog.x, sculk_fog.y, sculk_fog.z);

                } else if (SKY_CHUNK_LOADED || clientLevel.isLoaded(player.blockPosition())) { //SKY is always called first in vanilla, so we only need to check if the SKY flag is true, but just in case
                    TERRAIN_CHUNK_LOADED = true;
                    TERRAIN_FAR = event.getFarPlaneDistance() * 0.5F;
                    TERRAIN_NEAR =  TERRAIN_FAR * 0.75F;
                }
            }
        }
    }


}