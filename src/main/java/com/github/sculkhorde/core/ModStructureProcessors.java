package com.github.sculkhorde.core;

import com.github.sculkhorde.common.world.processors.WaterloggingFixProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModStructureProcessors {

    //TODO PORT 1.19.2 public static final DeferredRegister<StructureProcessorType<?>> PROCESSORS = DeferredRegister.create(ForgeRegistries.STRUCTURE_PROCESSOR, SculkHorde.MOD_ID);

    //TODO PORT 1.19.2 public static final RegistryObject<StructureProcessorType<WaterloggingFixProcessor>> WATERLOGGING_FIX_PROCESSOR = PROCESSORS.register("waterlogging_fix_processor", () -> () ->WaterloggingFixProcessor.CODEC);
}
