package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.goals;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.FloorSoulSpearsEntity;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class FloorSoulSpearsAttackGoal extends Goal
{
    private final SculkSoulReaperEntity mob;
    protected int maxAttackDuration = 0;
    protected int elapsedAttackDuration = 0;
    protected final int executionCooldown = TickUnits.convertSecondsToTicks(20);
    protected int ticksElapsed = executionCooldown;
    FloorSoulSpearsSpawner spawner;
    List<LivingEntity> enemies;
    ArrayList<FloorSoulSpearsSpawner> spawners = new ArrayList<>();
    protected long UPDATE_INTERVAL = TickUnits.convertSecondsToTicks(0.15F);
    protected long lastUpdate = 0;
    protected int minDifficulty = 0;
    protected int maxDifficulty = 0;


    public FloorSoulSpearsAttackGoal(SculkSoulReaperEntity mob, int durationInTicks, int minDifficulty, int maxDifficulty) {
        this.mob = mob;
        maxAttackDuration = durationInTicks;
        this.minDifficulty = minDifficulty;
        this.maxDifficulty = maxDifficulty;
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse()
    {
        ticksElapsed++;

        if(mob.getTarget() == null)
        {
            return false;
        }

        if(ticksElapsed < executionCooldown)
        {
            return false;
        }

        if(mob.getMobDifficultyLevel() < minDifficulty || mob.getMobDifficultyLevel() > maxDifficulty)
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean canContinueToUse()
    {
        return elapsedAttackDuration < maxAttackDuration;
    }

    @Override
    public void start()
    {
        super.start();
        enemies = EntityAlgorithms.getHostileEntitiesInBoundingBox((ServerLevel) mob.level(), mob.getBoundingBox().inflate(20));

        for(LivingEntity e : enemies)
        {
            spawners.add(new FloorSoulSpearsSpawner((ServerLevel) mob.level(), mob.blockPosition(), e));
        }
        //getEntity().triggerAnim("attack_controller", "fireball_sky_summon_animation");
        //getEntity().triggerAnim("twitch_controller", "fireball_sky_twitch_animation");
    }

    @Override
    public void tick()
    {
        super.tick();
        elapsedAttackDuration++;

        if(Math.abs(mob.level().getGameTime() - lastUpdate) < UPDATE_INTERVAL)
        {
            return;
        }

        lastUpdate = mob.level().getGameTime();

        for(FloorSoulSpearsSpawner spawner : spawners)
        {
            spawner.tick();
        }
    }

    @Override
    public void stop()
    {
        super.stop();
        elapsedAttackDuration = 0;
        ticksElapsed = 0;
        spawner = null;
    }


    public class FloorSoulSpearsSpawner {
        private final ServerLevel level;
        private final BlockPos origin;
        private final LivingEntity target;
        private final PriorityQueue<BlockPos> queue = new PriorityQueue<>(Comparator.comparingInt(this::heuristic));
        private boolean debugMode = false;
        private ArmorStand debugStand;
        private boolean pathFound = false;
        private boolean isFinished = false;

        private int MAX_DISTANCE = 150;


        public FloorSoulSpearsSpawner(ServerLevel level, BlockPos origin, LivingEntity target) {
            this.level = level;
            this.origin = origin;
            this.target = target;
            queue.add(origin);
        }

        public void enableDebugMode() {
            debugMode = true;
        }


        protected boolean isObstructed(ServerLevel level, BlockPos blockPos)
        {
            boolean isBlockAir = level.getBlockState(blockPos).is(Blocks.AIR);
            boolean isBlockNotExposedToAir = !BlockAlgorithms.isExposedToAir(level, blockPos);

            return isBlockAir || isBlockNotExposedToAir;
        }

        private int heuristic(BlockPos pos) {
            // Only consider x and z coordinates
            return Math.abs(pos.getX() - target.blockPosition().getX()) + Math.abs(pos.getZ() - target.blockPosition().getZ());
        }

        public void tick() {
            if (pathFound || queue.isEmpty() || target == null || target.isDeadOrDying())
            {

                if(pathFound && debugMode)
                {
                    SculkHorde.LOGGER.info("FloorSoulSpearsSpawner | Reached Target");
                }
                else if(debugMode)
                {
                    SculkHorde.LOGGER.info("FloorSoulSpearsSpawner | Did Not Target Block");
                }

                isFinished = true;
                return;
            }



            // Spawn Debug Stand if Necessary
            if(debugStand == null && debugMode)
            {
                debugStand = new ArmorStand(level, origin.getX(), origin.getY(), origin.getZ());
                debugStand.setInvisible(true);
                debugStand.setNoGravity(true);
                debugStand.addEffect(new MobEffectInstance(MobEffects.GLOWING, TickUnits.convertHoursToTicks(1), 3));
                level.addFreshEntity(debugStand);
            }

            BlockPos current = queue.poll();

            // Spawn Floor Soul Spear
            FloorSoulSpearsEntity entity = new FloorSoulSpearsEntity(mob, current.getX(), current.getY() + 1, current.getZ(), 0);
            mob.level().addFreshEntity(entity);

            if(debugMode)
            {
                debugStand.teleportTo(current.getX() + 0.5, current.getY(), current.getZ() + 0.5);
            }

            if (current == target.blockPosition()) {
                pathFound = true;
                return;
            }

            for (BlockPos neighbor : BlockAlgorithms.getNeighborsCube(current, false)) {

                if (isObstructed(level, neighbor)) {
                    continue;
                }

                if(neighbor.distManhattan(origin) > MAX_DISTANCE)
                {
                    continue;
                }

                queue.add(neighbor);

                if (debugMode) {
                    //level.setBlockAndUpdate(neighbor, Blocks.GREEN_STAINED_GLASS.defaultBlockState());
                }
            }
        }


        public boolean isPathFound() {
            return pathFound;
        }



        public void setMaxDistance(int value) {
            MAX_DISTANCE = value;
        }

        public boolean isFinished()
        {
            return isFinished;
        }

    }

}
