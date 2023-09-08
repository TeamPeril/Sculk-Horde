package com.github.sculkhorde.core;

import com.github.sculkhorde.common.effect.PurityEffect;
import com.github.sculkhorde.common.effect.SculkInfectionEffect;
import com.github.sculkhorde.common.effect.SculkLureEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMobEffects {

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, SculkHorde.MOD_ID);

    public static final RegistryObject<SculkInfectionEffect> SCULK_INFECTION = EFFECTS.register("sculk_infected", SculkInfectionEffect::new);

    public static final RegistryObject<SculkLureEffect> SCULK_LURE = EFFECTS.register("sculk_lure", SculkLureEffect::new);
    public static final RegistryObject<PurityEffect> PURITY = EFFECTS.register("purity", PurityEffect::new);

}
