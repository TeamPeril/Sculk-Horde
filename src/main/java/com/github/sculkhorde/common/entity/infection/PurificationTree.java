package com.github.sculkhorde.common.entity.infection;

import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class PurificationTree {
    private TreeNode root;
    private boolean Active = false;
    private final Direction direction;
    private CursorPurifierProberEntity cursorProbe;
    private CursorSurfacePurifierEntity cursorPurifier;
    private final ServerLevel world;
    private state currentState = state.IDLE;
    private enum state {
        IDLE,
        PROBING,
        PURIFICATION,
        COMPLETE
    }

    private BlockPos potentialNodePosition = null;
    private int failedProbeAttempts = 0;
    private final int MAX_FAILED_PROBE_ATTEMPTS = 2;
    private int currentProbeRange = 10;
    private final int MAX_PROBE_RANGE = 5000;
    private final int MIN_PROBE_RANGE = 10;
    private final int PROBE_RANGE_INCREMENT = 50;
    private final int MAX_PURIFIER_RANGE = 100;

    private BlockPos purificationTargetPosition = null;
    private int failedPurificationAttempts = 0;
    private final int MAX_FAILED_PURIFICATION_ATTEMPTS = 10;

    /**
     * Creates a new binary tree with the given value.
     */
    public PurificationTree(ServerLevel world, Direction direction, BlockPos rootPos)
    {
        this.root = new TreeNode(rootPos);
        this.direction = direction;
        this.world = world;
    }

    // Getters and Setters

    public boolean isActive() {
        return Active;
    }

    public void activate() {
        Active = true;
    }

    public void deactivate() {
        Active = false;
    }

    /**
     * Gets the root node.
     * @return The root node.
     */
    public TreeNode getRoot() {
        return root;
    }

    /**
     * Sets the root node.
     */
    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public void setOrigin(BlockPos origin)
    {
        this.root.blockPos = origin;
    }

    // Events

    /**
     * Creates a new probe cursor
     * @param maxDistance The maximum distance the cursor can travel
     */
    public void createProbeCursor(int maxDistance) {
        cursorProbe = new CursorPurifierProberEntity(world);
        cursorProbe.setMaxRange(maxDistance);
        cursorProbe.setPreferedDirection(direction);
        cursorProbe.setPos(this.root.blockPos.getX(), this.root.blockPos.getY(), this.root.blockPos.getZ());
        cursorProbe.setMaxTransformations(1);
        this.world.addFreshEntity(cursorProbe);
    }

    /**
     * Creates a new infection cursor
     * @param maxInfections The maximum number of infections the cursor can perform
     */
    public void createPurificationCursor(int maxInfections) {
        cursorPurifier = new CursorSurfacePurifierEntity(world);
        cursorPurifier.setPos(purificationTargetPosition.getX(), purificationTargetPosition.getY(), purificationTargetPosition.getZ());
        cursorPurifier.setMaxRange(maxInfections);
        cursorPurifier.setTickIntervalMilliseconds(10);
        this.world.addFreshEntity(cursorPurifier);
    }

    /**
     * Ticks the infection tree
     */
    public void tick()
    {
        // If the root is null, or the tree is not active, do nothing
        if(root.blockPos == BlockPos.ZERO || !isActive())
        {
            return;
        }
        if(SculkHorde.savedData == null) { return; }


        // If the probe has failed too many times, change state to complete
        if(failedProbeAttempts >= MAX_FAILED_PROBE_ATTEMPTS)
        {
            // Change State to Complete
            currentState = state.COMPLETE;
        }

        // If the probe range is too large, reset it
        if(currentProbeRange > MAX_PROBE_RANGE)
        {
            // Reset the probe range
            currentProbeRange = MIN_PROBE_RANGE;
        }

        if(currentState == state.IDLE)
        {
            currentState = state.PROBING;
        }
        else if(currentState == state.PROBING)
        {
            // If the probe is null, create a new one
            if(cursorProbe == null)
            {
                createProbeCursor(currentProbeRange);
                return;
            }
            // If the probe is still active, wait for it to finish
            else if(cursorProbe.isAlive())
            {
                return;
            }

            // If the probe is successful, record the findings
            if(cursorProbe.currentTransformations > 0)
            {
                potentialNodePosition = cursorProbe.blockPosition();
                failedProbeAttempts = 0;
                cursorProbe = null;
                // Change State to Infection Mode
                currentState = state.PURIFICATION;
            }
            // If the probe is not successful, record the findings
            else
            {
                cursorProbe = null;
                failedProbeAttempts++;
                potentialNodePosition = BlockPos.ZERO;
            }
        }
        else if(currentState == state.PURIFICATION)
        {
            purificationTargetPosition = potentialNodePosition;

            // If the infection cursor is null, create a new one
            if(cursorPurifier == null)
            {
                createPurificationCursor(MAX_PURIFIER_RANGE);
                return;
            }
            // If the infection cursor is still active, wait for it to finish
            else if(cursorPurifier.isAlive())
            {
                return;
            }

            // If the infection is successful, record the findings
            if(cursorPurifier.currentTransformations > 0)
            {
                failedPurificationAttempts = 0;
                cursorPurifier = null;
            }
            // If the infection is not successful, record the findings
            else
            {
                failedPurificationAttempts++;
                cursorPurifier = null;
            }

            // If the infection range is too large, reset it and change state to complete
            if(failedPurificationAttempts >= MAX_FAILED_PURIFICATION_ATTEMPTS)
            {
                failedPurificationAttempts = 0;
                currentState = state.PROBING;
            }
        }
        else if(currentState == state.COMPLETE)
        {
            if(failedProbeAttempts >= MAX_FAILED_PROBE_ATTEMPTS)
            {
                currentProbeRange += PROBE_RANGE_INCREMENT;
                failedProbeAttempts = 0;
                currentState = state.IDLE;
            }

            if(failedPurificationAttempts >= MAX_FAILED_PURIFICATION_ATTEMPTS)
            {
                currentState = state.IDLE;
                failedPurificationAttempts = 0;
            }
        }

    }

    /**
     * A node in a binary tree.
     */
    public class TreeNode {
        private BlockPos blockPos;
        private TreeNode left;
        private TreeNode right;

        public TreeNode(BlockPos blockPos) {
            this.blockPos = blockPos;
        }

        public Object getBlockPos() {
            return blockPos;
        }

        public void setBlockPos(BlockPos blockPos) {
            this.blockPos = blockPos;
        }

        public TreeNode getLeft() {
            return left;
        }

        public void setLeft(TreeNode left) {
            this.left = left;
        }

        public TreeNode getRight() {
            return right;
        }

        public void setRight(TreeNode right) {
            this.right = right;
        }
    }
}
