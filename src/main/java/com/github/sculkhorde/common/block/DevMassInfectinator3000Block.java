package com.github.sculkhorde.common.block;

import com.github.sculkhorde.common.blockentity.DevMassInfectinator3000BlockEntity;
import com.github.sculkhorde.core.*;
import com.github.sculkhorde.util.ChunkLoading.ChunkLoaderHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeBlock;

import javax.annotation.Nullable;
import java.util.List;

import static com.github.sculkhorde.core.ModBlockEntities.DEV_MASS_INFECTINATOR_3000_BLOCK_ENTITY;


/**
 * Chunk Loader Code created by SuperMartijn642
 */

public class DevMassInfectinator3000Block extends BaseEntityBlock implements IForgeBlock {
    /**
     * HARDNESS determines how difficult a block is to break<br>
     * 0.6f = dirt<br>
     * 1.5f = stone<br>
     * 2f = log<br>
     * 3f = iron ore<br>
     * 50f = obsidian
     */
    public static float HARDNESS = 5000f;

    /**
     * BLAST_RESISTANCE determines how difficult a block is to blow up<br>
     * 0.5f = dirt<br>
     * 2f = wood<br>
     * 6f = cobblestone<br>
     * 1,200f = obsidian
     */
    public static float BLAST_RESISTANCE = 1000f;

    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public DevMassInfectinator3000Block(Properties prop) {
        super(prop);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public DevMassInfectinator3000Block() {
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
                .mapColor(MapColor.COLOR_BLUE)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .sound(SoundType.ANCIENT_DEBRIS);
        return prop;
    }

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving){

        if(worldIn.isClientSide())
        {
            return;
        }
        ChunkLoaderHelper.getChunkLoaderHelper().createChunkLoadRequestSquareForEntityOrBlockPos(pos, 16, 1);
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if(worldIn.isClientSide())
        {
            return;
        }

        ChunkLoaderHelper.getChunkLoaderHelper().removeRequestsWithOwner(pos, (ServerLevel) worldIn);
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    /**
     * This is the description the item of the block will display when hovered over.
     * @param stack The item stack
     * @param iBlockReader A block reader
     * @param tooltip The tooltip
     * @param flagIn The flag
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter iBlockReader, List<Component> tooltip, TooltipFlag flagIn) {

        tooltip.add(Component.translatable("tooltip.sculkhorde.dev_mass_infectinator_3000"));
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, DEV_MASS_INFECTINATOR_3000_BLOCK_ENTITY.get(), DevMassInfectinator3000BlockEntity::tick);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        return new DevMassInfectinator3000BlockEntity(blockPos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

}
