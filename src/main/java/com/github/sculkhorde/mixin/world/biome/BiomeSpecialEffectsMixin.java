// Create a mixin to override getSkyColor() in BiomeSpecialEffects
package com.github.sculkhorde.mixin.world.biome;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(net.minecraft.world.level.biome.BiomeSpecialEffects.class)
public class BiomeSpecialEffectsMixin {

    /*
    // Inject at the start of the method and cancel original execution
    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void onGetSkyColor(CallbackInfoReturnable<Integer> cir) {
        // change this value to whatever sky color you want (RGB integer)
        int overriddenColor = ColorUtil.hexToInt(ColorUtil.sculkBaseColor1);

        cir.setReturnValue(overriddenColor);
    }

     */
}
