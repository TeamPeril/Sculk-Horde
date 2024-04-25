package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.entity.infection.CursorSurfaceInfectorEntity;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactory;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactoryEntry;
import com.github.sculkhorde.core.gravemind.entity_factory.ReinforcementRequest;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

import static com.github.sculkhorde.common.block.SculkMassBlock.WATERLOGGED;

public class SculkMassBlockEntity extends BlockEntity {

    /**
     * storedSculkMass is the value of sculk mass was this block has.
     * This value is used to determine the mobs that are spawened or the area
     * that will be infected by the sculk.
     * storedSculkMassIdentifier is the string used to identify storedSculkMass
     * in CompoundNBT. It allows us to read/write to it.<br>
     */
    protected int storedSculkMass = 0;
    protected String storedSculkMassIdentifier = "storedSculkMass";
    protected long lastTickTime = 0;
    protected int tickInterval = TickUnits.convertSecondsToTicks(3);


    /**
     * The Constructor that takes in properties
     */
    public SculkMassBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SCULK_MASS_BLOCK_ENTITY.get(), pos, state);
    }

    /**
     * Called when loading block entity from world.
     * @param compoundNBT Where NBT data is stored.
     */
    @Override
    public void load(CompoundTag compoundNBT) {
        super.load(compoundNBT);
        this.storedSculkMass = compoundNBT.getInt(storedSculkMassIdentifier);
    }

    /**
     * ???
     * @param compoundNBT Where NBT data is stored??
     * @return ???
     */
    @Override
    public void saveAdditional(CompoundTag compoundNBT) {

        compoundNBT.putInt(storedSculkMassIdentifier, this.storedSculkMass);
        super.saveAdditional(compoundNBT);
    }

    public int getStoredSculkMass()
    {
        return storedSculkMass;
    }

    public void setStoredSculkMass(int value)
    {
        storedSculkMass = Math.max(0, value);
    }

    public void addStoredSculkMass(int value)
    {
        storedSculkMass = Math.max(0, storedSculkMass + value);
    }


    public static void tick(Level level, BlockPos blockPos, BlockState blockState, SculkMassBlockEntity blockEntity)
    {
        // If world is not a server world, return
        if(level.isClientSide)
        {
            return;
        }
        // Tick every 10 seconds
        if(level.getGameTime() - blockEntity.lastTickTime < blockEntity.tickInterval)
        {
            return;
        }

        blockEntity.lastTickTime = level.getGameTime();

        if(blockEntity.getStoredSculkMass() <= 0)
        {
            level.destroyBlock(blockPos, false);
            return;
        }

        if(SculkHorde.populationHandler.isPopulationAtMax())
        {
            SculkHorde.savedData.addSculkAccumulatedMass(blockEntity.getStoredSculkMass());
            blockEntity.setStoredSculkMass(0);
            return;
        }

        // If we can spawn reinforcements

        EntityFactory entityFactory = SculkHorde.entityFactory;
        ReinforcementRequest context = new ReinforcementRequest((ServerLevel) level, blockPos);

        context.sender = ReinforcementRequest.senderType.SculkMass;
        context.budget = blockEntity.getStoredSculkMass();

        if(blockEntity.getBlockState().getValue(WATERLOGGED))
        {
            context.approvedStrategicValues.add(EntityFactoryEntry.StrategicValues.Aquatic);
        }

        //Attempt to call in reinforcements and then update stored sculk mass
        entityFactory.requestReinforcementSculkMass(level, blockPos, context);
        if(context.isRequestViewed && context.isRequestApproved)
        {
            blockEntity.setStoredSculkMass(context.remaining_balance);
        }

        // Do not spawn infectors if infection not enabled.
        if(!ModConfig.SERVER.block_infestation_enabled.get())
        {
            return;
        }

        // Spawn Block Traverser
        CursorSurfaceInfectorEntity cursor = new CursorSurfaceInfectorEntity(level);
        cursor.setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        cursor.setMaxTransformations(blockEntity.getStoredSculkMass() * 10);
        cursor.setMaxRange(blockEntity.getStoredSculkMass()* 10);
        level.addFreshEntity(cursor);
    }
}
