package com.github.sculkhorde.core;

import com.github.sculkhorde.common.world.processors.WaterloggingFixProcessor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModStructureProcessors {

    public static final DeferredRegister<StructureProcessorType<?>> PROCESSORS = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, SculkHorde.MOD_ID);

    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<WaterloggingFixProcessor>> WATERLOGGING_FIX_PROCESSOR = PROCESSORS.register("waterlogging_fix_processor", () -> () -> WaterloggingFixProcessor.CODEC);
}
