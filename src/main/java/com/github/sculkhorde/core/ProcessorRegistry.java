package com.github.sculkhorde.core;

import com.github.sculkhorde.common.world.processors.WaterloggingFixProcessor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ProcessorRegistry {

    public static final DeferredRegister<StructureProcessorType<?>> PROCESSORS = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, SculkHorde.MOD_ID);

    public static final RegistryObject<StructureProcessorType<WaterloggingFixProcessor>> WATERLOGGING_FIX_PROCESSOR = PROCESSORS.register("waterlogging_fix_processor", () -> () ->WaterloggingFixProcessor.CODEC);
}
