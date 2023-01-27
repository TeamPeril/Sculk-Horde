package com.github.sculkhoard.common.block;

import com.github.sculkhoard.common.tileentity.SculkBeeNestTile;
import com.github.sculkhoard.core.BlockRegistry;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;

public class SculkBeeNestBlock extends BeehiveBlock {

    public static final DirectionProperty FACING = HorizontalBlock.FACING;
    public static final BooleanProperty CLOSED = BooleanProperty.create("closed");
    public static final IntegerProperty HONEY_LEVEL = IntegerProperty.create("honey_level", 0, 5);
    /**
     * MATERIAL is simply what the block is made up. This affects its behavior & interactions.<br>
     * MAP_COLOR is the color that will show up on a map to represent this block
     */
    public static Material MATERIAL = Material.PLANT;
    public static MaterialColor MAP_COLOR = MaterialColor.COLOR_CYAN;

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
    public static float BLAST_RESISTANCE = 0.5f;

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
    public static int HARVEST_LEVEL = -1;

    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public SculkBeeNestBlock(AbstractBlock.Properties prop) {
        super(prop);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(HONEY_LEVEL, 0)
                .setValue(CLOSED, true));
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public SculkBeeNestBlock() {
        this(getProperties());
    }

    /**
     * Determines the properties of a block.<br>
     * I made this in order to be able to establish a block's properties from within the block class and not in the BlockRegistry.java
     * @return The Properties of the block
     */
    public static AbstractBlock.Properties getProperties()
    {
        return AbstractBlock.Properties.of(MATERIAL, MAP_COLOR)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .harvestTool(PREFERRED_TOOL)
                .harvestLevel(HARVEST_LEVEL)
                .sound(SoundType.SLIME_BLOCK)
                .noOcclusion()
                .noDrops();
    }

    public static boolean isNestClosed(BlockState blockState)
    {
        return blockState.hasProperty(CLOSED) && blockState.is(BlockRegistry.SCULK_BEE_NEST_BLOCK.get()) && blockState.getValue(CLOSED);
    }

    public static void setNestClosed(ServerWorld world, BlockState blockState, BlockPos position)
    {
        if(!blockState.hasProperty(CLOSED) || !blockState.is(BlockRegistry.SCULK_BEE_NEST_BLOCK.get())) { return; }
        world.setBlock(position, blockState.setValue(CLOSED, Boolean.valueOf(true)), 3);
    }

    public static void setNestOpen(ServerWorld world, BlockState blockState, BlockPos position)
    {
        if(!blockState.hasProperty(CLOSED) || !blockState.is(BlockRegistry.SCULK_BEE_NEST_BLOCK.get())) { return; }
        world.setBlock(position, blockState.setValue(CLOSED, Boolean.valueOf(false)), 3);
    }

    @Override
    public void onPlace(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving)
    {
        super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);

        //If world isnt client side and we are in the overworld
        if(!pLevel.isClientSide() && pLevel.equals(ServerLifecycleHooks.getCurrentServer().overworld()))
        {
            SculkHoard.gravemind.getGravemindMemory().addBeeNestToMemory(pPos);
        }
    }

    @Nullable
    public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
        return new SculkBeeNestTile();
    }


    /**
     * A function called by forge to create the tile entity.
     * @param state The current blockstate
     * @param world The world the block is in
     * @return Returns the tile entity.
     */
    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return newBlockEntity(world);
    }


    /**
     * Determines what the blockstate should be for placement.
     * @param context
     * @return
     */
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(HONEY_LEVEL, 0)
                .setValue(CLOSED, true);

    }


    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     * @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever possible. Implementing/overriding is
     * fine.
     */
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }


    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     * @deprecated call via {@link IBlockState#withMirror(Mirror)} whenever possible. Implementing/overriding is fine.
     */
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, CLOSED, HONEY_LEVEL);
    }
}
