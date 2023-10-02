package com.github.sculkhorde.core;

import com.github.sculkhorde.common.structures.SculkTombStructure;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Locale;


public class ModStructures {

    //TODO PORT 1.19.2 public static final DeferredRegister<StructureType<?>> STRUCTURES = DeferredRegister.create(ForgeRegistries.STRUCTURE_TYPE, SculkHorde.MOD_ID);

    //TODO PORT 1.19.2 public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, SculkHorde.MOD_ID);

    //TODO PORT 1.19.2 public static final RegistryObject<StructureType<SculkTombStructure>> SCULK_TOMB_STRUCTURE = STRUCTURES.register("sculk_tomb_structure", () -> explicitStructureTypeTyping(SculkTombStructure.CODEC));

    /**
     * Originally, I had a double lambda ()->()-> for the RegistryObject line above, but it turns out that
     * some IDEs cannot resolve the typing correctly. This method explicitly states what the return type
     * is so that the IDE can put it into the DeferredRegistry properly.
     */
    private static <T extends Structure> StructureType<T> explicitStructureTypeTyping(Codec<T> structureCodec) {
        return () -> structureCodec;
    }

    private static RegistryObject<StructurePieceType> registerStructurePiece(String name, StructurePieceType structurePieceType) {
        return null;//TODO PORT 1.19.2 STRUCTURE_PIECES.register(name.toLowerCase(Locale.ROOT), () -> structurePieceType);
    }
}
