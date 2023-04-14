package com.github.sculkhorde.common.block;

import com.github.sculkhorde.common.blockentity.SculkBeeNestBlockEntity;
import com.github.sculkhorde.core.BlockEntityRegistry;
import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SculkBeeNestBlock extends BeehiveBlock {

    public static final BooleanProperty CLOSED = BooleanProperty.create("closed");
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
    public static float HARDNESS = 4f;

    /**
     * BLAST_RESISTANCE determines how difficult a block is to blow up<br>
     * 0.5f = dirt<br>
     * 2f = wood<br>
     * 6f = cobblestone<br>
     * 1,200f = obsidian
     */
    public static float BLAST_RESISTANCE = 0.5f;

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
    public SculkBeeNestBlock(BlockBehaviour.Properties prop) {
        super(prop);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(HONEY_LEVEL, 0)
                .setValue(CLOSED, false));
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
    public static BlockBehaviour.Properties getProperties()
    {
        return BlockBehaviour.Properties.of(MATERIAL, MAP_COLOR)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .sound(SoundType.SLIME_BLOCK);
    }

    public static boolean isNestClosed(BlockState blockState)
    {
        return blockState.hasProperty(CLOSED) && blockState.is(BlockRegistry.SCULK_BEE_NEST_BLOCK.get()) && blockState.getValue(CLOSED);
    }

    public static void setNestClosed(ServerLevel world, BlockState blockState, BlockPos position)
    {
        if(!blockState.hasProperty(CLOSED) || !blockState.is(BlockRegistry.SCULK_BEE_NEST_BLOCK.get())) { return; }
        world.setBlock(position, blockState.setValue(CLOSED, Boolean.valueOf(true)), 3);
    }

    public static void setNestOpen(ServerLevel world, BlockState blockState, BlockPos position)
    {
        if(!blockState.hasProperty(CLOSED) || !blockState.is(BlockRegistry.SCULK_BEE_NEST_BLOCK.get())) { return; }
        world.setBlock(position, blockState.setValue(CLOSED, Boolean.valueOf(false)), 3);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving)
    {
        super.onPlace(state, level, pos, oldState, isMoving);

        //If world isnt client side and we are in the overworld
        if(!level.isClientSide() && level.dimension() == Level.OVERWORLD)
        {
            SculkHorde.gravemind.getGravemindMemory().addBeeNestToMemory(pos);
        }
    }


    /**
     * Determines what the blockstate should be for placement.
     * @param context
     * @return
     */
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(HONEY_LEVEL, 0)
                .setValue(CLOSED, false);

    }


    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     * @deprecated call via IBlockState#withRotation(Rotation) whenever possible. Implementing/overriding is
     * fine.
     */
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }


    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     * @deprecated call via IBlockState#withMirror(Mirror) whenever possible. Implementing/overriding is fine.
     */
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, CLOSED, HONEY_LEVEL);
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

        super.appendHoverText(stack, iBlockReader, tooltip, flagIn); //Not sure why we need this
        tooltip.add(Component.translatable("tooltip.sculkhorde.sculk_bee_nest")); //Text that displays if holding shift
    }

    // Block Entity Related

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, BlockEntityRegistry.SCULK_BEE_NEST_BLOCK_ENTITY.get(), SculkBeeNestBlockEntity::serverTick);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        return new SculkBeeNestBlockEntity(blockPos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

}
