package com.github.sculkhoard.common.block;

import com.github.sculkhoard.core.BlockRegistry;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Random;

import static com.github.sculkhoard.core.SculkHoard.DEBUG_MODE;

public class BlockAlgorithms {

    /**
     * Will return an array list that represents a 3x3x3 cube of all block
     * positions with the origin being the centroid. Does not include origin
     * in this list.
     * @param origin
     * @return
     */
    public static ArrayList<BlockPos> getNeighborsCube(BlockPos origin)
    {
        ArrayList<BlockPos> list = new ArrayList<BlockPos>();
        list.addAll(getNeighborsXZPlane(origin, false));
        list.addAll(getNeighborsXZPlane(origin.above(), true));
        list.addAll(getNeighborsXZPlane(origin.below(), true));
        return list;
    }

    /**
     * Will return an array list representing a 2D layer
     * @param origin
     * @return
     */
    public static ArrayList<BlockPos> getNeighborsXZPlane(BlockPos origin, boolean includeOrigin)
    {
        ArrayList<BlockPos> list = new ArrayList<BlockPos>();
        list.add(origin.north());
        list.add(origin.north().east());
        list.add(origin.north().west());
        list.add(origin.east());
        list.add(origin.south());
        list.add(origin.south().east());
        list.add(origin.south().west());
        list.add(origin.west());
        if(includeOrigin) list.add(origin);

        return list;
    }

    /**
     * Chooses a random neighbor position
     * @param origin The origin Block Position
     * @param serverWorld The ServerWorld of the block
     * @return The target Block Position
     */
    public static BlockPos getRandomNeighbor(ServerWorld serverWorld, BlockPos origin)
    {
        ArrayList<BlockPos> positions = getNeighborsCube(origin);
        return positions.get(serverWorld.random.nextInt(positions.size()));
    }

    /**
     * Will only place sculk nodes if sky is visible
     * @param world
     * @param targetPos
     */
    public static void placeSculkNode(ServerWorld world, BlockPos targetPos)
    {
        //If we are too close to another node, do not create one
        if(!SculkHoard.gravemind.isValidPositionForSculkNode(targetPos))
            return;

        //Given random chance and the target location can see the sky, create a sculk node
        if(new Random().nextInt(1000) <= 1 && world.canSeeSky(targetPos))
        {
            world.setBlockAndUpdate(targetPos, BlockRegistry.SCULK_BRAIN.get().defaultBlockState());
            SculkHoard.gravemind.sculkNodePositions.add(targetPos);
            EntityType.LIGHTNING_BOLT.spawn(world, null, null, targetPos, SpawnReason.SPAWNER, true, true);
            if(DEBUG_MODE) System.out.println("New Sculk Node Created at " + targetPos.toString());
        }

    }

    /**
     * A Jank solution to spawning flora. Given a random chance, spawn flora.
     * @param targetPos The BlockPos to spawn it at
     * @param world The world to spawn it in.
     */
    public static void placeSculkFlora(BlockPos targetPos, ServerWorld world)
    {

        ((SculkFloraBlock) SculkHoard.randomSculkFlora.getRandomEntry()).placeBlockHere(world, targetPos);

    }

    /**
     * Will place random flora attached to a given position.
     * @param serverWorld the world
     * @param origin the position
     */
    public static void placeFloraAroundLog(ServerWorld serverWorld, BlockPos origin) {
        boolean DEBUG_THIS = false;
        VeinBlock vein = BlockRegistry.VEIN.get();

        BlockPos[] possiblePositions = {
                origin.north(),
                origin.east(),
                origin.south(),
                origin.west()
        };

        //50% chance to place sculk vein for each face
        for(BlockPos pos : possiblePositions)
        {
            if(serverWorld.random.nextInt(10) < 3 &&
                    serverWorld.getBlockState(pos).isAir())
            {
                vein.placeBlock(serverWorld, pos);
            }
        }
    }

    /**
     * Places a line of sculk vein above a block. Length and height of line is random.
     * @param serverWorld the world
     * @param origin the block we want to place these above
     */
    public static void placePatchesOfVeinAbove(ServerWorld serverWorld, BlockPos origin)
    {
        int OFFSET_MAX = 3;
        int LENGTH_MAX = 5;
        int LENGTH_MIN = 3;

        Random rng = new Random();
        int offset = rng.nextInt(OFFSET_MAX);
        int length = rng.nextInt(LENGTH_MAX - LENGTH_MIN) + LENGTH_MIN;
        VeinBlock vein = BlockRegistry.VEIN.get();

        //Attempt to place sculk vein in a straight line above origin
        BlockPos indexPos = origin.above(offset);
        for(int i = 0; i < length; i++)
        {
            indexPos = indexPos.above();

            //75% chance to place vein
            if(serverWorld.random.nextInt(4) <= 2)
            {
                vein.placeBlock(serverWorld, indexPos);
            }
        }
    }
}
