package com.github.sculkhorde.core;

import com.github.sculkhorde.common.effect.*;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModMobEffects {

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, SculkHorde.MOD_ID);
    public static final DeferredHolder<MobEffect, SculkBurrowedEffect> SCULK_INFECTION = EFFECTS.register("sculk_infected", SculkBurrowedEffect::new);
    public static final DeferredHolder<MobEffect, SculkLureEffect> SCULK_LURE = EFFECTS.register("sculk_lure", SculkLureEffect::new);
    public static final DeferredHolder<MobEffect, PurityEffect> PURITY = EFFECTS.register("purity", PurityEffect::new);
    public static final DeferredHolder<MobEffect, DiseasedCystsEffect> DISEASED_CYSTS = EFFECTS.register("diseased_cysts", DiseasedCystsEffect::new);
    public static final DeferredHolder<MobEffect, SculkVesselEffect> SCULK_VESSEL = EFFECTS.register("sculk_vessel", SculkVesselEffect::new);
    public static final DeferredHolder<MobEffect, CorrodingEffect> CORRODED = EFFECTS.register("corroded", CorrodingEffect::new);
    public static final DeferredHolder<MobEffect, DenseEffect> DENSE = EFFECTS.register("dense", DenseEffect::new);
    public static final DeferredHolder<MobEffect, DiseasedAtmosphereEffect> DISEASED_ATMOSPHERE = EFFECTS.register("diseased_atmosphere", DiseasedAtmosphereEffect::new);
    public static final DeferredHolder<MobEffect, SculkFogEffect> SCULK_FOG = EFFECTS.register("sculk_fog", SculkFogEffect::new);
    public static final DeferredHolder<MobEffect, RootedEffect> ROOTED_EFFECT = EFFECTS.register("rooted", RootedEffect::new);

}
