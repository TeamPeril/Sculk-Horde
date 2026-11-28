package com.github.sculkhorde.systems;

import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.ModSounds;
import com.github.sculkhorde.util.PlayerProfileHandler;
import com.github.sculkhorde.util.SoundUtil;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;

public class AmbientSFXSystem {
    ArrayList<ServerPlayer> players;

    protected long lastTimeOfExecution = 0;
    protected int populationRecountInterval = TickUnits.convertSecondsToTicks(10);

    public AmbientSFXSystem()
    {

    }


    public void serverTick()
    {
        long currentTime = ServerLifecycleHooks.getCurrentServer().overworld().getGameTime();

        // I saw a weird bug where the lastTimeOfPopulationRecount was bigger than currentTime. No Idea why.
        // Therefore I will use math.abs
        if(Math.abs(currentTime - lastTimeOfExecution) >= populationRecountInterval)
        {
            lastTimeOfExecution = currentTime;
            playAmbientSounds();
        }
    }



    public void playAmbientSounds()
    {
        // We want to make a copy and shuffle it because otherwise, the same players will be getting played sounds.
        players = new ArrayList<ServerPlayer>(ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers());
        Collections.shuffle(players);

        for(ServerPlayer player : players)
        {
            // Get Surrounding Blocks
            List<BlockInfo> blocks = getSurroundingBlockStatesAndPositions(player, 20);

            // If soulite is present, play sound
            Optional<BlockPos> temp = getSoulitePos(blocks);
            if(temp.isPresent())
            {
                SoundUtil.playAmbientSoundInLevel(player.level(), temp.get(), ModSounds.SOULITE_AMBIENCE.get());
                PlayerProfileHandler.setTimeUntilNextAmbientSound(player, TickUnits.convertSecondsToTicks(60));
                PlayerProfileHandler.setTimeOfLastAmbientSound(player, player.level().getGameTime());
                return;
            }

            // Anything above this ignores cooldown. Everything below follows cooldown
            if(!PlayerProfileHandler.isTimeForNextAmbientSound(player, player.level().getGameTime()))
            {
                continue;
            }

            temp = getInfestedBlocksPos(blocks);
            if(temp.isPresent() && !PlayerProfileHandler.isPlayerActiveVessel(player))
            {
                SoundUtil.playAmbientSoundInLevel(player.level(), temp.get(), ModSounds.INFESTATION_AMBIENCE.get());
                PlayerProfileHandler.setTimeUntilNextAmbientSound(player, TickUnits.convertMinutesToTicks(15));
                PlayerProfileHandler.setTimeOfLastAmbientSound(player, player.level().getGameTime());
                return;
            }

        }
    }

    public List<BlockInfo> getSurroundingBlockStatesAndPositions(ServerPlayer player, int range) {
        Level level = player.level();
        Vec3 playerPos = player.position();
        List<BlockInfo> blocks = new ArrayList<>();

        // Define the 14 directions (6 cardinal + 8 diagonals)
        Vec3[] directions = {
                // Cardinal directions
                new Vec3(0, 0, -1),  // North
                new Vec3(0, 0, 1),   // South
                new Vec3(-1, 0, 0),  // West
                new Vec3(1, 0, 0),   // East
                new Vec3(0, 1, 0),   // Up
                new Vec3(0, -1, 0),  // Down
                // Diagonals (NW, NE, SW, SE, etc.)
                new Vec3(-1, 0, -1), // NW
                new Vec3(1, 0, -1),  // NE
                new Vec3(-1, 0, 1),  // SW
                new Vec3(1, 0, 1),   // SE
                new Vec3(0, 1, -1),  // Up-North
                new Vec3(0, 1, 1),   // Up-South
                new Vec3(0, -1, -1), // Down-North
                new Vec3(0, -1, 1),  // Down-South
        };

        for (Vec3 dir : directions) {
            Vec3 endPos = playerPos.add(dir.scale(range));
            BlockHitResult result = level.clip(new ClipContext(
                    playerPos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player
            ));
            if (result.getType() != HitResult.Type.MISS) {
                BlockPos hitPos = result.getBlockPos();
                BlockState state = level.getBlockState(hitPos);
                blocks.add(new BlockInfo(hitPos, state));
            }
        }
        Collections.shuffle(blocks);
        return blocks;
    }



    protected static Optional<BlockPos> getSoulitePos(List<BlockInfo> list)
    {
        Optional<BlockPos> pos = Optional.empty();
        for(int index = 0; index < list.size(); index++)
        {
            if(containsSoulite(list.get(index)))
            {
                pos = Optional.of(list.get(index).pos);
            }
        }
        return pos;
    }

    protected static boolean containsSoulite(BlockInfo blockInfo)
    {
        return blockInfo.contains(ModBlocks.SOULITE_BLOCK.get())
                || blockInfo.contains(ModBlocks.SOULITE_BUD_BLOCK.get())
                || blockInfo.contains(ModBlocks.SOULITE_CLUSTER_BLOCK.get())
                || blockInfo.contains(ModBlocks.DEPLETED_SOULITE_BLOCK.get())
                || blockInfo.contains(ModBlocks.BUDDING_SOULITE_BLOCK.get());
    }

    protected static Optional<BlockPos> getInfestedBlocksPos(List<BlockInfo> list)
    {
        Optional<BlockPos> pos = Optional.empty();
        for(int index = 0; index < list.size(); index++)
        {
            if(containsInfestedBlocks(list.get(index)))
            {
                pos = Optional.of(list.get(index).pos);
            }
        }
        return pos;
    }

    protected static boolean containsInfestedBlocks(BlockInfo blockInfo)
    {
        return blockInfo.contains(ModBlocks.BlockTags.INFESTED_BLOCK);
    }

    protected static class BlockInfo
    {
        public BlockPos pos;
        public BlockState state;

        BlockInfo(BlockPos pos, BlockState state)
        {
            this.pos = pos;
            this.state = state;
        }

        public boolean contains(Block block)
        {
            return state.is(block);
        }

        public boolean contains(TagKey<Block> tag)
        {
            return state.is(tag);
        }
    }

}
