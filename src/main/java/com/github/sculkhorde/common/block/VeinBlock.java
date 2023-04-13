package com.github.sculkhorde.common.block;

import com.github.sculkhorde.core.BlockRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeBlock;

import javax.annotation.Nullable;
import java.util.List;

import static com.github.sculkhorde.core.SculkHorde.DEBUG_MODE;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class VeinBlock extends VineBlock implements IForgeBlock {

    /**
     * MATERIAL is simply what the block is made up. This affects its behavior & interactions.<br>
     * MAP_COLOR is the color that will show up on a map to represent this block
     */
    public static Material MATERIAL = Material.PLANT;
    public static MaterialColor MAP_COLOR = MaterialColor.COLOR_BLUE;

    /**
     * HARDNESS determines how difficult a block is to break<br>
     * 0.6f = dirt<br>
     * 1.5f = stone<br>
     * 2f = log<br>
     * 3f = iron ore<br>
     * 50f = obsidian
     */
    public static float HARDNESS = 0.6f;

    /**
     * BLAST_RESISTANCE determines how difficult a block is to blow up<br>
     * 0.5f = dirt<br>
     * 2f = wood<br>
     * 6f = cobblestone<br>
     * 1,200f = obsidian
     */
    public static float BLAST_RESISTANCE = 0.6f;

    /**
     *  Harvest Level Affects what level of tool can mine this block and have the item drop<br>
     *
     *  -1 = All<br>
     *  0 = Wood<br>
     *  1 = Stone<br>
     *  2 = Iron<br>
     *  3 = Diamond<br>
     *  4 = Netherite
     */
    public static int HARVEST_LEVEL = 3;


    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public VeinBlock(Properties prop) {
        super(prop);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public VeinBlock() {
        this(getProperties());
    }

    /**
     * Determines the properties of a block.<br>
     * I made this in order to be able to establish a block's properties from within the block class and not in the BlockRegistry.java
     * @return The Properties of the block
     */
    public static Properties getProperties()
    {
        return Properties.of(MATERIAL, MAP_COLOR)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .sound(SoundType.VINE)
                .noOcclusion()
                .noCollission();
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos)
    {
        Level worldIn = (Level) pLevel;
        //IF the face it is placed on is not a valid face, return false.
        //The face depends on the direction of the block
        BlockState northBlock = worldIn.getBlockState(pPos.north());
        BlockState eastBlock = worldIn.getBlockState(pPos.east());
        BlockState southBlock = worldIn.getBlockState(pPos.south());
        BlockState westBlock = worldIn.getBlockState(pPos.west());

        if(pState.getBlock().equals(BlockRegistry.VEIN.get()))
        {
            if(pState.getValue(NORTH) == true && isValidFace(worldIn, northBlock, pPos, Direction.SOUTH))
            {
                return true;
            }
            else if(pState.getValue(EAST) == true && isValidFace(worldIn, eastBlock, pPos, Direction.WEST))
            {
                return true;
            }
            else if(pState.getValue(SOUTH) == true && isValidFace(worldIn, southBlock, pPos, Direction.NORTH))
            {
                return true;
            }
            else if(pState.getValue(WEST) == true && isValidFace(worldIn, westBlock, pPos, Direction.EAST))
            {
                return true;
            }
        }
        else
        {
            if(isValidFace(worldIn, northBlock, pPos, Direction.SOUTH)
                || isValidFace(worldIn, eastBlock, pPos, Direction.WEST)
                || isValidFace(worldIn, southBlock, pPos, Direction.NORTH)
                || isValidFace(worldIn, westBlock, pPos, Direction.EAST))
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
        Block vein = BlockRegistry.VEIN.get();
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
    public boolean isValidFace(Level worldIn, BlockState blockState, BlockPos blockPosIn, Direction direction)
    {
        if(!blockState.isFaceSturdy(worldIn, blockPosIn, direction))
        {
            return false;
        }
        else if(blockState.is(BlockRegistry.CRUST.get()))
        {
            return false;
        }
        else if(blockState.is(BlockRegistry.SCULK_BEE_NEST_BLOCK.get()))
        {
            return false;
        }
        else if(blockState.is(BlockRegistry.SCULK_SUMMONER_BLOCK.get()))
        {
            return false;
        }
        else if(blockState.is(BlockRegistry.SCULK_LIVING_ROCK_BLOCK.get()))
        {
            return false;
        }
        else if(blockState.is(BlockRegistry.SCULK_NODE_BLOCK.get()))
        {
            return false;
        }
        else if(blockState.is(BlockRegistry.INFESTED_STONE_DORMANT.get()))
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
        tooltip.add(Component.literal("tooltip.sculkhorde.vein")); //Text that displays if holding shift

    }
}
