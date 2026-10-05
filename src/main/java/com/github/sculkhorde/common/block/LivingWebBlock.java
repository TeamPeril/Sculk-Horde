package com.github.sculkhorde.common.block;

import com.github.sculkhorde.util.EntityAlgorithms;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SculkVeinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LivingWebBlock extends SculkVeinBlock {
    public LivingWebBlock(Properties properties) {
        super(properties);
    }

    public LivingWebBlock()
    {
        this(getProperties());
    }

    public static Properties getProperties()
    {
        return Properties.ofFullCopy(Blocks.SCULK_VEIN);
    }

    @Override
    public boolean canBeReplaced(BlockState p_222381_, BlockPlaceContext p_222382_) {
        return true;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {

        if(entity instanceof LivingEntity livingEntity)
        {
            if(EntityAlgorithms.isSculkLivingEntity.test(livingEntity))
            {
                return;
            }

            entity.makeStuckInBlock(state, new Vec3(0.75F, 1F, 0.75F));
        }
    }
}
