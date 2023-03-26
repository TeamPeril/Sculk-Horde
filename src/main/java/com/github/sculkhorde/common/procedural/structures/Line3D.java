package com.github.sculkhorde.common.procedural.structures;

import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.function.Consumer;

public class Line3D {
    private Vector3d start; // The starting point of the line
    private Vector3d end; // The end point of the line
    private Vector3d direction; // The direction of the line

    // Constructor taking in two Vector3D points
    public Line3D(Vector3d start, Vector3d end) {
        this.start = start;
        this.end = end;
        this.direction = end.subtract(start); // Calculate the direction of the line
    }

    // Optional constructor taking in a BlockPos
    public Line3D(BlockPos start, BlockPos end) {
        this(new Vector3d(start.getX(), start.getY(), start.getZ()), new Vector3d(end.getX(), end.getY(), end.getZ()));
    }

    // Method to get the length of the line
    public double getLength() {
        return direction.length(); // The length of the line is the magnitude of the direction vector
    }



    // Method to iterate over the length of the line with a given step size
    /* Example of using the iterateOverLine method

       // This method will iterate over the line with a step size of 1 and set the block at each point to air
       line.iterateOverLine(1, (point) -> {
            BlockPos pos = new BlockPos(point.x, point.y, point.z);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
        });

     */
    /**
     * Iterate over the length of the line with a given step size
     * @param stepSize The step size to use when iterating over the line
     * @param consumer The consumer to use when iterating over the line
     */
    public void iterateOverLine(double stepSize, Consumer<Vector3d> consumer) {
        double length = getLength();
        double offset = 0.0;
        while (offset < length) {
            // Get the point on the line from the start plus the offset
            Vector3d point = start.add(direction.normalize().scale(offset));
            consumer.accept(point); // Use the consumer to do something with the point
            offset += stepSize; // Increment the parameter value
        }
    }

    /**
     * Get a list of block positions within a sphere along the line
     * @param radius The radius of the sphere
     * @return A list of block positions within a sphere along the line
     */
    public ArrayList<BlockPos> getBlockPositionsOnLineWithSphere(double radius) {
        ArrayList<BlockPos> blockPosList = new ArrayList<>();

        // Iterate over the line with a step size of 1
        iterateOverLine(1.0, point -> {
            int x = MathHelper.floor(point.x);
            int y = MathHelper.floor(point.y);
            int z = MathHelper.floor(point.z);

            for (int i = -MathHelper.floor(radius); i <= MathHelper.floor(radius); i++) {
                for (int j = -MathHelper.floor(radius); j <= MathHelper.floor(radius); j++) {
                    for (int k = -MathHelper.floor(radius); k <= MathHelper.floor(radius); k++) {
                        double distanceSq = (double)(i * i + j * j + k * k);
                        if (distanceSq <= radius * radius) {
                            blockPosList.add(new BlockPos(x + i, y + j, z + k));
                        }
                    }
                }
            }
        });

        return blockPosList;
    }
}
