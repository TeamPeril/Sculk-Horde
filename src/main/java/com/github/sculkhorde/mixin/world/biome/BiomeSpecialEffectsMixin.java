// Create a mixin to override getSkyColor() in BiomeSpecialEffects
package com.github.sculkhorde.mixin.world.biome;

import com.github.sculkhorde.util.ColorUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.level.biome.BiomeSpecialEffects.class)
public class BiomeSpecialEffectsMixin {
    // Inject at the start of the method and cancel original execution
    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void onGetSkyColor(CallbackInfoReturnable<Integer> cir) {
        // TODO: change this value to whatever sky color you want (RGB integer)
        int overriddenColor = ColorUtil.hexToInt(ColorUtil.sculkBaseColor1); // example pale blue

        // If you'd prefer to base it off the original field, you could do something like:
        // int overriddenColor = this.skyColor; // or modify it

        cir.setReturnValue(overriddenColor);
    }
}
