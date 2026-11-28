package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.StructureUtil;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;


public class StructureCoreBlockEntity extends BlockEntity
{
    StructureUtil.StructurePlacer structurePlacer;
    protected long tickedAt = 0;

    protected String structureResourceLocation = "sculkhorde:test_soulite_structure";

    ArrayList<String> structureVariants = new ArrayList<>();

    /**
     * The Constructor that takes in properties
     * @param blockPos The Position
     * @param blockState The Properties
     */
    public StructureCoreBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        this(ModBlockEntities.STRUCTURE_CORE_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    public StructureCoreBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState)
    {
        super(blockEntityType, blockPos, blockState);
        loadStructureVariants();
        setRandomStructureVariant();
    }


    /** Accessors **/

    protected int getBlockPlacementCooldown()
    {
        return TickUnits.convertSecondsToTicks(0.2F);
    }

    protected BlockState getBlockToConvertToAfterBuilding()
    {
        return ModBlocks.BUDDING_SOULITE_BLOCK.get().defaultBlockState();
    }

    protected void setRandomStructureVariant()
    {
        Collections.shuffle(structureVariants);
        setStructureResourceLocation(structureVariants.get(0));
    }

    /** Modifiers **/

    public void setStructureResourceLocation(String value)
    {
        structureResourceLocation = value;
    }

    protected void addStructureVariant(String str)
    {
        structureVariants.add(str);
    }


    /** Events **/

    protected void loadStructureVariants()
    {
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, StructureCoreBlockEntity blockEntity)
    {
        if(level.isClientSide()) { return; }

        ServerLevel serverLevel = (ServerLevel) level;

        if(Math.abs(level.getGameTime() - blockEntity.tickedAt) < blockEntity.getBlockPlacementCooldown())
        {
            return;
        }

        blockEntity.tickedAt = level.getGameTime();

        if(blockEntity.structurePlacer == null)
        {
            ResourceLocation structure = ResourceLocation.parse(blockEntity.structureResourceLocation);
            StructureTemplateManager structuretemplatemanager = serverLevel.getStructureManager();
            Optional<StructureTemplate> structureTemplate;
            structureTemplate = structuretemplatemanager.get(structure);

            StructurePlaceSettings structureplacesettings = (new StructurePlaceSettings());
            blockEntity.structurePlacer = new StructureUtil.StructurePlacer(structureTemplate.get(), serverLevel, blockPos, blockPos, structureplacesettings, serverLevel.getRandom());
            blockEntity.structurePlacer.appendIgnoreBlockPosList(blockPos);
        }

        if(blockEntity.structurePlacer.isFinished() && blockEntity.getBlockToConvertToAfterBuilding() != null)
        {
            BlockAlgorithms.setBlockStructure(level, blockPos, blockEntity.getBlockToConvertToAfterBuilding());
            return;
        }

        blockEntity.structurePlacer.tick();
    }
}
