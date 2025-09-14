package com.github.sculkhorde.util.hitboxes;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Predicate;

public interface IModHitbox {

    boolean intersects(Entity entity);

    List<Entity> getEntitiesInHitbox(Level level, Entity owner, Predicate<Entity> predicate);
    List<LivingEntity> getLivingEntitiesInHitbox(Level level, LivingEntity owner, Predicate<LivingEntity> predicate);

}
