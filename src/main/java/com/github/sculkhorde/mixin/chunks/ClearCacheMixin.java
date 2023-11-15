package com.github.sculkhorde.mixin.chunks;


import net.minecraft.server.level.ServerChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

//@Mixin(ServerChunkCache.class)
public abstract class ClearCacheMixin implements IClearCache {
    //@Shadow
    private void clearCache() {}

    public void publicClearCache() {
        clearCache();
    }
}