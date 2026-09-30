package com.github.sculkhorde;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(SculkHorde.MOD_ID)
public class SculkHorde {
    public static final String MOD_ID = "sculkhorde";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SculkHorde(IEventBus modEventBus, ModContainer container) {

    }
}
