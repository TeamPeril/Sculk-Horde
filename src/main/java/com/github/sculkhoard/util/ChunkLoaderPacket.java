package com.github.sculkhoard.util;

import com.github.sculkhoard.common.tileentity.SculkBrainTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created by SuperMartijn642
 */
public abstract class ChunkLoaderPacket {

    protected BlockPos pos;

    public ChunkLoaderPacket(BlockPos pos){
        this.pos = pos;
    }

    public ChunkLoaderPacket(PacketBuffer buffer){
        this.decodeBuffer(buffer);
    }

    public void encode(PacketBuffer buffer){
        buffer.writeBlockPos(this.pos);
    }

    protected void decodeBuffer(PacketBuffer buffer){
        this.pos = buffer.readBlockPos();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier){
        contextSupplier.get().setPacketHandled(true);

        PlayerEntity player = contextSupplier.get().getSender();
        if(player == null || player.blockPosition().distSqr(this.pos) > 32 * 32)
            return;
        World world = player.level;
        if(world == null)
            return;
        TileEntity tile = world.getBlockEntity(this.pos);
        if(tile instanceof SculkBrainTile)
            contextSupplier.get().enqueueWork(() -> this.handle(player, world, (SculkBrainTile)tile));
    }

    protected abstract void handle(PlayerEntity player, World world, SculkBrainTile tile);
}
