package com.github.sculkhoard.common.block;

import com.github.sculkhoard.core.BlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.VineBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeBlock;

import javax.annotation.Nullable;
import java.util.List;

import static com.github.sculkhoard.core.SculkHoard.DEBUG_MODE;

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
     * PREFERRED_TOOL determines what type of tool will break the block the fastest and be able to drop the block if possible
     */
    public static ToolType PREFERRED_TOOL = ToolType.HOE;

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
                .harvestTool(PREFERRED_TOOL)
                .harvestLevel(HARVEST_LEVEL)
                .sound(SoundType.VINE)
                .noOcclusion()
                .noCollission();
    }

    /**
     * Will attempt to place the sculk vein if there is a solid wall.
     * @param worldIn The world to place it in
     * @param blockPosIn The desired position
     */
    public void placeBlock(World worldIn, BlockPos blockPosIn)
    {
        if(worldIn.getBlockState(blockPosIn).isAir())
        {
            Block vein = BlockRegistry.VEIN.get();
            BlockState northBlock = worldIn.getBlockState(blockPosIn.north());
            BlockState eastBlock = worldIn.getBlockState(blockPosIn.east());
            BlockState southBlock = worldIn.getBlockState(blockPosIn.south());
            BlockState westBlock = worldIn.getBlockState(blockPosIn.west());

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
    public boolean isValidFace(World worldIn, BlockState blockState, BlockPos blockPosIn, Direction direction)
    {
        if(!blockState.isFaceSturdy(worldIn, blockPosIn, direction))
        {
            return false;
        }
        else if(BlockRegistry.INFECTED_DIRT.get().isValidVictim(blockState))
        {
            return false;
        }
        else if(blockState.is(BlockRegistry.CRUST.get()))
        {
            return false;
        }
        else if(blockState.is(BlockRegistry.INFECTED_DIRT.get()))
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
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader iBlockReader, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        super.appendHoverText(stack, iBlockReader, tooltip, flagIn); //Not sure why we need this
        tooltip.add(new TranslationTextComponent("tooltip.sculkhoard.vein")); //Text that displays if holding shift

    }

    /**
     * Returns the state that this block should transform into when right clicked by a tool.
     * For example: Used to determine if an axe can strip, a shovel can path, or a hoe can till.
     * Return null if vanilla behavior should be disabled.
     *
     * @param state The current state
     * @param world The world
     * @param pos The block position in world
     * @param player The player clicking the block
     * @param stack The stack being used by the player
     * @return The resulting state after the action has been performed
     */
    public BlockState getToolModifiedState(BlockState state, World world, BlockPos pos, PlayerEntity player, ItemStack stack, ToolType toolType)
    {
        if(DEBUG_MODE) System.out.println("Hi I am a Vine Block :)");

        return null; //Just Return null because We Are Not Modifying it
    }
}
