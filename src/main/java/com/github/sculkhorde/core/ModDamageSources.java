package com.github.sculkhorde.core;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

public class ModDamageSources {
    public static final ResourceKey<DamageType> CORRODED_DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(SculkHorde.MOD_ID, "corroded"));

    public static DamageSource corroded(Entity target, @Nullable Entity attacker) {
        return new DamageSource(target.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(CORRODED_DAMAGE_TYPE), attacker);
    }
}
