package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.util.StructureUtil;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;


public class StructureCoreBlockEntity extends BlockEntity
{
    StructureUtil.StructurePlacer structurePlacer;
    protected long tickedAt = 0;

    protected long blockPlacementCooldown = TickUnits.convertSecondsToTicks(0.5F);
    protected String structureResourceLocation = "sculkhorde:test_soulite_structure";

    protected BlockState blockToConvertToAfterBuilding = Blocks.DIRT.defaultBlockState();


    /**
     * The Constructor that takes in properties
     * @param blockPos The Position
     * @param blockState The Properties
     */
    public StructureCoreBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        super(ModBlockEntities.STRUCTURE_CORE_BLOCK_ENTITY.get(), blockPos, blockState);
    }


    /** Accessors **/

    public void setStructureResourceLocation(String value)
    {
        structureResourceLocation = value;
    }

    public void setBlockPlacementCooldown(long value)
    {
        blockPlacementCooldown = value;
    }

    public void setBlockToConvertToAfterBuilding(BlockState blockState)
    {
        blockToConvertToAfterBuilding = blockState;
    }

    /** Modifiers **/


    /** Events **/

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, StructureCoreBlockEntity blockEntity)
    {
        if(level.isClientSide()) { return; }

        ServerLevel serverLevel = (ServerLevel) level;

        if(Math.abs(level.getGameTime() - blockEntity.tickedAt) < blockEntity.blockPlacementCooldown)
        {
            return;
        }

        blockEntity.tickedAt = level.getGameTime();

        if(blockEntity.structurePlacer == null)
        {
            ResourceLocation structure = new ResourceLocation(blockEntity.structureResourceLocation);
            StructureTemplateManager structuretemplatemanager = serverLevel.getStructureManager();
            Optional<StructureTemplate> structureTemplate;
            structureTemplate = structuretemplatemanager.get(structure);

            StructurePlaceSettings structureplacesettings = (new StructurePlaceSettings());
            blockEntity.structurePlacer = new StructureUtil.StructurePlacer(structureTemplate.get(), serverLevel, blockPos, blockPos, structureplacesettings, serverLevel.getRandom());
            blockEntity.structurePlacer.appendIgnoreBlockPosList(blockPos);
        }

        if(blockEntity.structurePlacer.isFinished() && blockEntity.blockToConvertToAfterBuilding != null)
        {
            level.setBlockAndUpdate(blockPos, blockEntity.blockToConvertToAfterBuilding);
            return;
        }

        blockEntity.structurePlacer.tick();
    }
}
