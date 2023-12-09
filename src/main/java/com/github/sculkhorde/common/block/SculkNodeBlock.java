package com.github.sculkhorde.common.block;

import com.github.sculkhorde.common.blockentity.SculkNodeBlockEntity;
import com.github.sculkhorde.common.entity.SculkPhantomEntity;
import com.github.sculkhorde.core.*;
import com.github.sculkhorde.core.gravemind.Gravemind;
import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeBlock;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static com.github.sculkhorde.util.BlockAlgorithms.getBlockDistance;


/**
 * Chunk Loader Code created by SuperMartijn642
 */

public class SculkNodeBlock extends BaseEntityBlock implements IForgeBlock {
    /**
     * HARDNESS determines how difficult a block is to break<br>
     * 0.6f = dirt<br>
     * 1.5f = stone<br>
     * 2f = log<br>
     * 3f = iron ore<br>
     * 50f = obsidian
     */
    public static float HARDNESS = 50f;

    /**
     * BLAST_RESISTANCE determines how difficult a block is to blow up<br>
     * 0.5f = dirt<br>
     * 2f = wood<br>
     * 6f = cobblestone<br>
     * 1,200f = obsidian
     */
    public static float BLAST_RESISTANCE = 10f;

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public SculkNodeBlock(Properties prop) {
        super(prop);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(ACTIVE, false));
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public SculkNodeBlock() {
        this(getProperties());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState()
                .setValue(ACTIVE, false);

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(ACTIVE);
    }

    /**
     * Will only place sculk nodes if sky is visible
     * @param worldIn The World to place it in
     * @param targetPos The position to place it in
     */
    public static void tryPlaceSculkNode(ServerLevel worldIn, BlockPos targetPos, boolean forcePlace)
    {
        final int SPAWN_NODE_COST = 3000;
        final int SPAWN_NODE_BUFFER = 1000;

        boolean failRandomChance = new Random().nextInt(1000) > 1;
        boolean isSavedDataNull = SculkHorde.savedData == null;
        if(isSavedDataNull) {
            SculkHorde.LOGGER.error("Tried to place Node. SculkHorde.savedData is null");
            return;
        }

        if(forcePlace) {
            SculkNodeBlock.PlaceNode(worldIn, targetPos);
            return;
        }

        if(failRandomChance) { return; }

        boolean isTheHordeDefeated = SculkHorde.savedData.isHordeDefeated();
        boolean isNodeSpawnOnCooldown = !SculkHorde.savedData.isNodeSpawnCooldownOver();
        boolean isNotValidPositionForSculkNode = !isValidPositionForSculkNode(worldIn, targetPos);
        boolean isNotEnoughMass = SculkHorde.savedData.getSculkAccumulatedMass() < SPAWN_NODE_COST + SPAWN_NODE_BUFFER;
        boolean doNotSpawnNode = isTheHordeDefeated || isNodeSpawnOnCooldown || isNotValidPositionForSculkNode || isNotEnoughMass;

        if(doNotSpawnNode) { return; }

        SculkNodeBlock.PlaceNode(worldIn, targetPos);
        SculkHorde.savedData.subtractSculkAccumulatedMass(SPAWN_NODE_COST);

    }

    /**
     * Will check each known node location in {@link ModSavedData}
     * to see if there is one too close.
     * @param positionIn The potential location of a new node
     * @return true if creation of new node is approved, false otherwise.
     */
    public static boolean isValidPositionForSculkNode(ServerLevel worldIn, BlockPos positionIn)
    {
        if(SculkHorde.savedData == null) { return false;}
        if(SculkHorde.savedData.getNodeEntries().size() >= SculkHorde.gravemind.sculk_node_limit)
        {
            return false;
        }

        // Need to be far away from ancient node at 0,0
        if(worldIn.equals(ServerLifecycleHooks.getCurrentServer().overworld()) && BlockAlgorithms.getBlockDistanceXZ(positionIn, BlockPos.ZERO) < Gravemind.MINIMUM_DISTANCE_BETWEEN_NODES)
        {
            return false;
        }

        for (ModSavedData.NodeEntry entry : SculkHorde.savedData.getNodeEntries())
        {
            if(!entry.isEntryValid()) { continue; }
            if(!BlockAlgorithms.areTheseDimensionsEqual(entry.getDimension().dimension(), worldIn.dimension()))
            {
                continue;
            }

            //Get Distance from our potential location to the current index node position
            int distanceFromPotentialToCurrentNode = (int) getBlockDistance(positionIn, entry.getPosition());

            //if we find a single node that is too close, disapprove of creating a new one
            if (distanceFromPotentialToCurrentNode < Gravemind.MINIMUM_DISTANCE_BETWEEN_NODES)
            {
                return false;
            }
        }

        if(BlockAlgorithms.isNearFluid(worldIn, positionIn, 5))
        {
            return false;
        }

        return true;
    }

    public static void PlaceNode(ServerLevel level, BlockPos blockPos)
    {
        BlockPos newOrigin = new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        level.setBlockAndUpdate(newOrigin, ModBlocks.SCULK_NODE_BLOCK.get().defaultBlockState());
        SculkHorde.savedData.addNodeToMemory(level, newOrigin);
        SculkHorde.savedData.resetNoNodeSpawningTicksElapsed();
        EntityType.LIGHTNING_BOLT.spawn(level, newOrigin, MobSpawnType.SPAWNER);
        //Send message to all players that node has spawned
        level.players().forEach(player -> player.displayClientMessage(Component.literal("A Sculk Node has spawned!"), true));
        // Play sound for each player
        level.players().forEach(player -> level.playSound(null, player.blockPosition(), ModSounds.NODE_SPAWN_SOUND.get(), SoundSource.HOSTILE, 1.0F, 1.0F));
        spawnSculkPhantomsAtTopOfWorld(level, newOrigin, 10);
    }

    private static void spawnSculkPhantomsAtTopOfWorld(ServerLevel level, BlockPos origin, int amount)
    {
        int spawnRange = 100;
        int minimumSpawnRange = 50;
        Random rng = new Random();
        for(int i = 0; i < amount; i++)
        {
            int x = minimumSpawnRange + rng.nextInt(spawnRange) - (spawnRange/2);
            int z = minimumSpawnRange + rng.nextInt(spawnRange) - (spawnRange/2);
            int y = level.getMaxBuildHeight();
            BlockPos spawnPosition = new BlockPos(origin.getX() + x, y, origin.getZ() + z);

            SculkPhantomEntity.spawnPhantom(level, spawnPosition, true);

        }
    }

    /**
     * This function is called when this block is placed. <br>
     * @param world The world the block is in
     * @param bp The position the block is in
     * @param blockState The state of the block
     * @param entity The entity that placed it
     * @param itemStack The item stack it was placed from
     */
    @Override
    public void setPlacedBy(Level world, BlockPos bp, BlockState blockState, @Nullable LivingEntity entity, ItemStack itemStack)
    {
        super.setPlacedBy(world, bp, blockState, entity, itemStack);

        if(world.isClientSide()) { return; }

        //If world isnt client side and we are in the overworld
        if(!world.isClientSide())
        {
            SculkHorde.savedData.addNodeToMemory((ServerLevel) world, bp);
        }
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
                .sound(SoundType.GRASS);
        return prop;
    }

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving){

        if(worldIn.isClientSide())
        {
            // Display Text On Player Screens
            for (Player player : worldIn.players()) {
                player.displayClientMessage(Component.translatable("message.sculk_horde.node_placed"), true);
            }
        }

    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if(newState.getBlock() == this)
        {
            return;
        }

        if(worldIn.isClientSide())
        {
            return;
        }
        SculkHorde.savedData.removeNodeFromMemory(pos);
        // Subtract 10% of total mass
        int subtractAmount = (int) (SculkHorde.savedData.getSculkAccumulatedMass() * 0.1);
        SculkHorde.savedData.subtractSculkAccumulatedMass(subtractAmount);
        worldIn.players().forEach(player -> player.displayClientMessage(Component.literal("A Sculk Node has been Destroyed! " + subtractAmount + " Mass has been removed from the Horde."), true));
        worldIn.players().forEach(player -> worldIn.playSound(null, player.blockPosition(), ModSounds.NODE_DESTROY_SOUND.get(), SoundSource.HOSTILE, 0.7F, 1.0F));


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

        super.appendHoverText(stack, iBlockReader, tooltip, flagIn); //Not sure why we need this
        tooltip.add(Component.translatable("tooltip.sculkhorde.sculk_node")); //Text that displays if holding shift
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.SCULK_NODE_BLOCK_ENTITY.get(), SculkNodeBlockEntity::tick);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        return new SculkNodeBlockEntity(blockPos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

}
