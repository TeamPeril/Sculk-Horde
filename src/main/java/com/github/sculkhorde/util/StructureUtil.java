package com.github.sculkhorde.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;


public class StructureUtil {

    private static final DynamicCommandExceptionType ERROR_TEMPLATE_INVALID = new DynamicCommandExceptionType((p_214582_) -> {
        return Component.translatable("commands.place.template.invalid", p_214582_);
    });

    private static void checkLoaded(ServerLevel p_214544_, ChunkPos p_214545_, ChunkPos p_214546_) throws CommandSyntaxException {
        if (ChunkPos.rangeClosed(p_214545_, p_214546_).filter((p_214542_) -> {
            return !p_214544_.isLoaded(p_214542_.getWorldPosition());
        }).findAny().isPresent()) {
            throw BlockPosArgument.ERROR_NOT_LOADED.create();
        }
    }

    public static boolean placeStructureTemplate(ServerLevel level, ResourceLocation structure, BlockPos pos) throws CommandSyntaxException {
        StructureTemplateManager structuretemplatemanager = level.getStructureManager();
        Optional<StructureTemplate> optional;
        try {
            optional = structuretemplatemanager.get(structure);
        } catch (ResourceLocationException resourcelocationexception) {
            throw ERROR_TEMPLATE_INVALID.create(structure);
        }

        if (optional.isEmpty()) {
            throw ERROR_TEMPLATE_INVALID.create(structure);
        }

        StructureTemplate structuretemplate = optional.get();
        checkLoaded(level, new ChunkPos(pos), new ChunkPos(pos.offset(structuretemplate.getSize())));
        StructurePlaceSettings structureplacesettings = (new StructurePlaceSettings());

        boolean wasAbleToPlaceStructure = structuretemplate.placeInWorld(level, pos, pos, structureplacesettings, StructureBlockEntity.createRandom((long)0), 2);

        return false;

    }
}
