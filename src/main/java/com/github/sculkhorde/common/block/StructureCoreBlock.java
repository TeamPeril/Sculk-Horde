package com.github.sculkhorde.common.block;

import com.github.sculkhorde.common.blockentity.StructureCoreBlockEntity;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.extensions.IForgeBlock;

import javax.annotation.Nullable;

/**
 * Chunk Loader Code created by SuperMartijn642
 */

public class StructureCoreBlock extends BaseEntityBlock implements IForgeBlock {

    /**
     * HARDNESS determines how difficult a block is to break<br>
     * 0.6f = dirt<br>
     * 1.5f = stone<br>
     * 2f = log<br>
     * 3f = iron ore<br>
     * 50f = obsidian
     */
    public static float HARDNESS = 3f;

    /**
     * BLAST_RESISTANCE determines how difficult a block is to blow up<br>
     * 0.5f = dirt<br>
     * 2f = wood<br>
     * 6f = cobblestone<br>
     * 1,200f = obsidian
     */
    public static float BLAST_RESISTANCE = 6f;


    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public StructureCoreBlock(Properties prop) {
        super(prop);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public StructureCoreBlock() {
        this(getProperties());
    }


    /**
     * Determines if this block will randomly tick or not.
     * @param blockState The current blockstate
     * @return True/False
     */
    @Override
    public boolean isRandomlyTicking(BlockState blockState) {
        return false;
    }


    /**
     * Determines the properties of a block.<br>
     * I made this in order to be able to establish a block's properties from within the block class and not in the BlockRegistry.java
     * @return The Properties of the block
     */
    public static Properties getProperties()
    {
        Properties prop = Properties.of()
                .mapColor(MapColor.COLOR_CYAN)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .noLootTable()
                .sound(SoundType.AMETHYST);
        return prop;
    }


    // Block Entity Related

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, ModBlockEntities.STRUCTURE_CORE_BLOCK_ENTITY.get(), StructureCoreBlockEntity::tick);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        StructureCoreBlockEntity blockEntity = new StructureCoreBlockEntity(blockPos, state);
        blockEntity.setStructureResourceLocation("sculkhorde:test_soulite_structure");
        blockEntity.setBlockPlacementCooldown(TickUnits.convertSecondsToTicks(0.2F));
        blockEntity.setBlockToConvertToAfterBuilding(ModBlocks.BUDDING_SOULITE_BLOCK.get().defaultBlockState());
        return blockEntity;
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

}
