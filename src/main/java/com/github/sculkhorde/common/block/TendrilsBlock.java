package com.github.sculkhorde.common.block;

import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockInfestationHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeBlock;

import javax.annotation.Nullable;
import java.util.List;

public class TendrilsBlock extends VineBlock implements IForgeBlock {
    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public TendrilsBlock(Properties prop) {
        super(prop);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public TendrilsBlock() {
        this(getProperties());
    }

    /**
     * Determines the properties of a block.<br>
     * I made this in order to be able to establish a block's properties from within the block class and not in the BlockRegistry.java
     * @return The Properties of the block
     */
    public static Properties getProperties()
    {
        return Properties.copy(Blocks.VINE)
                .mapColor(MapColor.TERRACOTTA_BLUE)
                .noOcclusion()
                .noCollission();
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos)
    {
        //IF the face it is placed on is not a valid face, return false.
        //The face depends on the direction of the block
        BlockState northBlock = pLevel.getBlockState(pPos.north());
        BlockState eastBlock = pLevel.getBlockState(pPos.east());
        BlockState southBlock = pLevel.getBlockState(pPos.south());
        BlockState westBlock = pLevel.getBlockState(pPos.west());

        if(pState.getBlock().equals(ModBlocks.TENDRILS.get()))
        {
            if(pState.getValue(NORTH) == true && isValidFace(pLevel, northBlock, pPos, Direction.SOUTH))
            {
                return true;
            }
            else if(pState.getValue(EAST) == true && isValidFace(pLevel, eastBlock, pPos, Direction.WEST))
            {
                return true;
            }
            else if(pState.getValue(SOUTH) == true && isValidFace(pLevel, southBlock, pPos, Direction.NORTH))
            {
                return true;
            }
            else if(pState.getValue(WEST) == true && isValidFace(pLevel, westBlock, pPos, Direction.EAST))
            {
                return true;
            }
        }
        else
        {
            if(isValidFace(pLevel, northBlock, pPos, Direction.SOUTH)
                || isValidFace(pLevel, eastBlock, pPos, Direction.WEST)
                || isValidFace(pLevel, southBlock, pPos, Direction.NORTH)
                || isValidFace(pLevel, westBlock, pPos, Direction.EAST))
            {
                return true;
            }
        }
        return false;

    }

    /**
     * Will attempt to place the sculk vein if there is a solid wall.
     * @param worldIn The world to place it in
     * @param blockPosIn The desired position
     */
    public void placeBlock(Level worldIn, BlockPos blockPosIn)
    {
        // If the block is not air, return
        if(!worldIn.getBlockState(blockPosIn).isAir()) {
            return;
        }

        // If the block cannot survive, return
        if(!canSurvive(worldIn.getBlockState(blockPosIn), worldIn, blockPosIn))
        {
            return;
        }

        // Get the blocks around the block
        Block vein = ModBlocks.TENDRILS.get();
        BlockState northBlock = worldIn.getBlockState(blockPosIn.north());
        BlockState eastBlock = worldIn.getBlockState(blockPosIn.east());
        BlockState southBlock = worldIn.getBlockState(blockPosIn.south());
        BlockState westBlock = worldIn.getBlockState(blockPosIn.west());

        // If the block is valid, place it
        if(isValidFace(worldIn, northBlock, blockPosIn, Direction.SOUTH))
        {
            worldIn.setBlockAndUpdate(blockPosIn, vein.defaultBlockState().setValue(NORTH, true));
        }
        else if(isValidFace(worldIn, eastBlock, blockPosIn, Direction.WEST))
        {
            worldIn.setBlockAndUpdate(blockPosIn, vein.defaultBlockState().setValue(EAST, true));
        }
        else if(isValidFace(worldIn, southBlock, blockPosIn, Direction.NORTH))
        {
            worldIn.setBlockAndUpdate(blockPosIn, vein.defaultBlockState().setValue(SOUTH, true));
        }
        else if(isValidFace(worldIn, westBlock, blockPosIn, Direction.EAST))
        {
            worldIn.setBlockAndUpdate(blockPosIn, vein.defaultBlockState().setValue(WEST, true));
        }

    }

    /**
     * Determines if a sculk vein can be placed on a block. <br>
     * It won't be placed if face isn't sturdy or if infected dirt can spread to it.
     * @param worldIn The world
     * @param blockState The blockstate of the target block
     * @param blockPosIn The position of the block
     * @param direction The direction of the face
     * @return
     */
    public boolean isValidFace(LevelReader worldIn, BlockState blockState, BlockPos blockPosIn, Direction direction)
    {
        if(!blockState.isFaceSturdy(worldIn, blockPosIn, direction))
        {
            return false;
        }
        else if(BlockInfestationHelper.isCurable((ServerLevel) worldIn, blockPosIn))
        {
            return false;
        }
        else if(blockState.is(ModBlocks.SCULK_BEE_NEST_BLOCK.get()))
        {
            return false;
        }
        else if(blockState.is(ModBlocks.SCULK_SUMMONER_BLOCK.get()))
        {
            return false;
        }
        else if(blockState.is(ModBlocks.SCULK_LIVING_ROCK_BLOCK.get()))
        {
            return false;
        }
        else if(blockState.is(ModBlocks.SCULK_NODE_BLOCK.get()))
        {
            return false;
        }
        else if(blockState.is(ModBlocks.INFESTED_STONE.get()))
        {
            return false;
        }
        return true;
    }

    /**
     * This is the description the item of the block will display when hovered over.
     * @param stack The item stack
     * @param iBlockReader ???
     * @param tooltip ???
     * @param flagIn ???
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter iBlockReader, List<Component> tooltip, TooltipFlag flagIn) {

        super.appendHoverText(stack, iBlockReader, tooltip, flagIn); //Not sure why we need this
        tooltip.add(Component.translatable("tooltip.sculkhorde.tendrils")); //Text that displays if holding shift

    }
}
