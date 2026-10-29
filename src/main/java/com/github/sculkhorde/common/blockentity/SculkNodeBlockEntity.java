package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.block.SculkNodeBlock;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.gravemind_system.Gravemind;
import com.github.sculkhorde.systems.infestation_systems.node_infestation.NodeAtmosphereInfestationSystem;
import com.github.sculkhorde.systems.infestation_systems.node_infestation.NodeBranchingInfestationSystem;
import com.github.sculkhorde.common.structures.procedural.SculkNodeProceduralStructure;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.systems.node_system.SculkNodeSelectorNode;
import com.github.sculkhorde.systems.node_system.SculkNodeSpreadBehavior;
import com.github.sculkhorde.util.ChunkLoading.BlockEntityChunkLoaderHelper;
import com.github.sculkhorde.util.DifficultyUtil;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.PlayerProfileHandler;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.TimeUnit;

/**
 * Chunkloader code created by SuperMartijn642
 */
public class SculkNodeBlockEntity extends BlockEntity
{
    protected long tickedAt = System.nanoTime();
    protected SculkNodeProceduralStructure nodeProceduralStructure;
    protected final long REPAIR_INTERVAL_TICKS = TickUnits.convertHoursToTicks(1);
    protected long timeOfLastRepair = -1;
    public static final int chunkLoadIntervalTicks = TickUnits.convertMinutesToTicks(5);
    public long timeOfLastChunkLoadTick = 0;


    protected int currentInfestationRadius = 0;
    protected String currentInfestationRadiusIdentifier = "currentInfestationRadius";
    protected int timeOfLastAtmosphereInfestation = 0;
    protected String timeOfLastAtmosphereInfestationIdentifier = "timeOfLastAtmosphereInfestation";
    protected long creationTime = 0;
    protected String creationTimeID = "creationTime";
    public boolean isBeingMoved = false;

    protected final long heartBeatDelayMillis = TimeUnit.SECONDS.toMillis(10);
    protected long lastHeartBeat = System.currentTimeMillis();

    SculkNodeSelectorNode selectorNode;

    public SculkNodeBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.SCULK_NODE_BLOCK_ENTITY.get(), blockPos, blockState);
        selectorNode = new SculkNodeSelectorNode(this);
        selectorNode.addChild(new SculkNodeSpreadBehavior(this));
    }

    /** Accessors **/

    public boolean isActive()
    {
        return this.getBlockState().getValue(SculkNodeBlock.ACTIVE);
    }

    public void setActive(boolean active)
    {
        this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(SculkNodeBlock.ACTIVE, active), 3);
    }

    public long getCreationTime()
    {
        return creationTime;
    }


    /** Modifiers **/


    /** Events **/

    protected static void addDarknessEffectToNearbyPlayers(Level level, BlockPos blockPos, int distance)
    {
        level.players().forEach((player) -> {
            if(player.blockPosition().closerThan(blockPos, distance) && !player.isCreative() && !player.isInvulnerable() && !player.isSpectator() && !PlayerProfileHandler.isPlayerVessel(player))
            {
                EntityAlgorithms.applyEffectToTarget(player, MobEffects.DARKNESS, TickUnits.convertMinutesToTicks(1), 0);
            }
        });
    }


    public static void tick(Level level, BlockPos blockPos, BlockState blockState, SculkNodeBlockEntity blockEntity)
    {
        if(!Gravemind.isGravemindActive())
        {
            return;
        }

        blockEntity.coreSystemsTick();

        if(level.isClientSide)
        {
            return;
        }

        blockEntity.selectorNode.execute();
    }

    public void coreSystemsTick()
    {
        if(creationTime == 0)
        {
            creationTime = level.getGameTime();
        }

        if(level.isClientSide)
        {
            if(System.currentTimeMillis() - lastHeartBeat > heartBeatDelayMillis)
            {
                lastHeartBeat = System.currentTimeMillis();
                level.playLocalSound(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.BLOCKS, 5.0F, 1.0F, false);
            }
            return;
        }

        if(TickUnits.hasTicksPassed(timeOfLastChunkLoadTick, level, chunkLoadIntervalTicks)) {
            // Update the tickedAt time
            tickedAt = System.nanoTime();
            chunkloadTick(this);
        }

        addDarknessEffectToNearbyPlayers(level, getBlockPos(), 50);
        repairNodeTick(this);
    }

    protected static void chunkloadTick(SculkNodeBlockEntity blockEntity)
    {
        if(blockEntity.isActive())
        {
            BlockEntityChunkLoaderHelper.getChunkLoaderHelper().createChunkLoadRequestSquare((ServerLevel) blockEntity.getLevel(), blockEntity.getBlockPos(), ModConfig.SERVER.sculk_node_chunkload_radius.get(), 1, TickUnits.convertMinutesToTicks(30));
        }
        else
        {
            BlockEntityChunkLoaderHelper.getChunkLoaderHelper().removeRequestsWithOwner(blockEntity.getBlockPos(), (ServerLevel) blockEntity.getLevel());
        }
    }

    protected static void repairNodeTick(SculkNodeBlockEntity blockEntity)
    {
        /** Building Shell Process **/
        //If the structure has not been initialized yet, do it
        if(blockEntity.nodeProceduralStructure == null)
        {
            //Create Structure
            blockEntity.nodeProceduralStructure = new SculkNodeProceduralStructure((ServerLevel) blockEntity.getLevel(), blockEntity.getBlockPos());
            blockEntity.nodeProceduralStructure.generatePlan();
        }

        //If currently building, call build tick.
        if(blockEntity.nodeProceduralStructure.isCurrentlyBuilding())
        {
            blockEntity.nodeProceduralStructure.buildTick();
            blockEntity.timeOfLastRepair = blockEntity.getLevel().getGameTime();
        }
        //If enough time has passed, or we haven't built yet, and we can build, start build
        else if((TickUnits.hasTicksPassed(blockEntity.timeOfLastRepair, blockEntity.getLevel(), blockEntity.REPAIR_INTERVAL_TICKS) || blockEntity.timeOfLastRepair <= 0) && blockEntity.nodeProceduralStructure.canStartToBuild())
        {
            blockEntity.nodeProceduralStructure.startBuildProcedure();
        }
    }



    /**
     * Called when loading block entity from world.
     * @param compoundNBT Where NBT data is stored.
     */
    @Override
    public void load(CompoundTag compoundNBT) {
        super.load(compoundNBT);
        this.currentInfestationRadius = compoundNBT.getInt(currentInfestationRadiusIdentifier);
        this.timeOfLastAtmosphereInfestation = compoundNBT.getInt(timeOfLastAtmosphereInfestationIdentifier);
        this.creationTime = compoundNBT.getLong(creationTimeID);
    }

    @Override
    public void saveAdditional(CompoundTag compoundNBT) {

        compoundNBT.putInt(currentInfestationRadiusIdentifier, this.currentInfestationRadius);
        compoundNBT.putInt(timeOfLastAtmosphereInfestationIdentifier, this.timeOfLastAtmosphereInfestation);
        compoundNBT.putLong(creationTimeID, this.creationTime);
        super.saveAdditional(compoundNBT);
    }

    public int getCurrentInfestationRadius()
    {
        return currentInfestationRadius;
    }

    public void setCurrentInfestationRadius(int value)
    {
        currentInfestationRadius = Math.max(0, value);
    }

    public void incrementCurrentInfestationRadius(int value)
    {
        setCurrentInfestationRadius(Math.max(0, getCurrentInfestationRadius() + value));
    }

    public int getTimeOfLastAtmosphereInfestation()
    {
        return timeOfLastAtmosphereInfestation;
    }

    public void setTimeOfLastAtmosphereInfestation(int value)
    {
        timeOfLastAtmosphereInfestation = value;
    }
}
