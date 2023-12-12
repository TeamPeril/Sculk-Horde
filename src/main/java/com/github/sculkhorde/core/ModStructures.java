package com.github.sculkhorde.core;

import java.util.Locale;

import com.github.sculkhorde.common.structures.SculkTombStructure;
import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;


public class ModStructures {

    public static final DeferredRegister<StructureType<?>> STRUCTURES = DeferredRegister.create(Registry.STRUCTURE_TYPE_REGISTRY, SculkHorde.MOD_ID);

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES = DeferredRegister.create(Registry.STRUCTURE_PIECE_REGISTRY, SculkHorde.MOD_ID);

    public static final RegistryObject<StructureType<SculkTombStructure>> SCULK_TOMB_STRUCTURE = STRUCTURES.register("sculk_tomb_structure", () -> explicitStructureTypeTyping(SculkTombStructure.CODEC));

    /**
     * Originally, I had a double lambda ()->()-> for the RegistryObject line above, but it turns out that
     * some IDEs cannot resolve the typing correctly. This method explicitly states what the return type
     * is so that the IDE can put it into the DeferredRegistry properly.
     */
    private static <T extends Structure> StructureType<T> explicitStructureTypeTyping(Codec<T> structureCodec) {
        return () -> structureCodec;
    }

    private static RegistryObject<StructurePieceType> registerStructurePiece(String name, StructurePieceType structurePieceType) {
        return STRUCTURE_PIECES.register(name.toLowerCase(Locale.ROOT), () -> structurePieceType);
    }
}
