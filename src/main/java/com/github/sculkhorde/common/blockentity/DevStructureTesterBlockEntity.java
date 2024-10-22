package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.structures.procedural.ProceduralStructure;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.util.StructureUtil;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;

/**
 * Chunkloader code created by SuperMartijn642
 */
public class DevStructureTesterBlockEntity extends BlockEntity
{
    StructureUtil.StructurePlacer structurePlacer;
    public long tickedAt = 0;

    public long TICK_COOLDOWN = TickUnits.convertSecondsToTicks(0.5F);

    private ProceduralStructure proceduralStructure;

    private int spawnedCursors = 0;
    private int maxSPawned = 1000;


    /**
     * The Constructor that takes in properties
     * @param blockPos The Position
     * @param blockState The Properties
     */
    public DevStructureTesterBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        super(ModBlockEntities.DEV_STRUCTURE_TESTER_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    /** Accessors **/


    /** Modifiers **/


    /** Events **/

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, DevStructureTesterBlockEntity blockEntity)
    {
        if(level.isClientSide()) { return; }

        ServerLevel serverLevel = (ServerLevel) level;

        if(Math.abs(level.getGameTime() - blockEntity.tickedAt) < blockEntity.TICK_COOLDOWN)
        {
            return;
        }

        blockEntity.tickedAt = level.getGameTime();

        if(blockEntity.structurePlacer == null)
        {
            ResourceLocation structure = new ResourceLocation("sculkhorde:test_soulite_structure");
            StructureTemplateManager structuretemplatemanager = serverLevel.getStructureManager();
            Optional<StructureTemplate> structureTemplate;
            structureTemplate = structuretemplatemanager.get(structure);

            StructurePlaceSettings structureplacesettings = (new StructurePlaceSettings());
            blockEntity.structurePlacer = new StructureUtil.StructurePlacer(structureTemplate.get(), serverLevel, blockPos, blockPos, structureplacesettings, serverLevel.getRandom());
            blockEntity.structurePlacer.appendIgnoreBlockPosList(blockPos);
        }

        blockEntity.structurePlacer.tick();
    }
}
