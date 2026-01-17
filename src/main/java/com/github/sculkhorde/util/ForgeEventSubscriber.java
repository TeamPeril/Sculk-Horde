package com.github.sculkhorde.util;

import com.github.sculkhorde.common.advancement.ContributeTrigger;
import com.github.sculkhorde.common.block.FleshyCompostBlock;
import com.github.sculkhorde.common.effect.IPotionExpireEffect;
import com.github.sculkhorde.core.*;
import com.github.sculkhorde.systems.infestation_systems.block_infestation_system.BlockInfestationSystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.function.Predicate;


@Mod.EventBusSubscriber(modid = SculkHorde.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventSubscriber {

    @SubscribeEvent
    public static void WorldLoadEvent(LevelEvent.Load event)
    {
        BlockInfestationSystem.explicitInfectableBlockEntityBlocks.addEntry(0, Blocks.CRAFTING_TABLE, ModBlocks.INFESTED_CRAFTING_TABLE_BLOCK.get().defaultBlockState());
    }


    /**
     * Gets Called Every tick when a world is running.
     * @param event The event with all the details
     */
    @SubscribeEvent
    public static void WorldTickEvent(TickEvent.LevelTickEvent event)
    {
        // If we are on client, or we are not in the overworld, return
        if(event.level.isClientSide() || (event.phase == TickEvent.Phase.END) || !event.level.equals(ServerLifecycleHooks.getCurrentServer().overworld()))
        {
            return;
        }

        if(SculkHorde.gravemind == null)
        {
            ModSavedData.initializeData();
            return;
        }

        if(!SculkHorde.gravemind.isWorldFullyLoaded)
        {
            SculkHorde.gravemind.isWorldFullyLoaded = true;
        }

        SculkHorde.gravemind.serverTick();
    }


    @SubscribeEvent
    public static void ServerTickEvent(TickEvent.ServerTickEvent event)
    {
        // If we are on client, or we are not in the overworld, return
        if((event.phase == TickEvent.Phase.END))
        {
            return;
        }

        if(SculkHorde.gravemind == null)
        {
            //ModSavedData.InitializeData();
        }
    }

    @SubscribeEvent
    public static void onLivingEntityDeathEvent(LivingDeathEvent event)
    {
        if(event.getEntity().level().isClientSide())
        {
            return;
        }

        if(EntityAlgorithms.isSculkLivingEntity.test(event.getEntity()))
        {
            ModSavedData.getSaveData().reportDeath((ServerLevel) event.getEntity().level(), event.getEntity().blockPosition());
            ModSavedData.getSaveData().addHostileToMemory(event.getEntity().getLastHurtByMob());
            SculkHorde.statisticsData.incrementTotalUnitDeaths();
            SculkHorde.statisticsData.addTotalMassRemovedFromHorde((int) event.getEntity().getMaxHealth());
            return;

        }

        Entity killerEntity = event.getSource().getEntity();
        if(killerEntity instanceof LivingEntity killerLivingEntity)
        {
            if(EntityAlgorithms.isSculkLivingEntity.test(killerLivingEntity))
            {
                FleshyCompostBlock.placeBlock(event.getEntity());
            }
        }

        // If a player kills an entity (That is not sculk)
        if(killerEntity instanceof ServerPlayer player)
        {
            if(EntityAlgorithms.isSculkLivingEntity.test(event.getEntity()))
            {
                return;
            }

            InventoryUtil.repairIHealthRepairableItemStacks(player.getInventory(), (int) event.getEntity().getMaxHealth());
        }

    }

    @SubscribeEvent
    public static void onPotionExpireEvent(MobEffectEvent.Expired event)
    {
        if(event.getEntity().level().isClientSide() || SculkHorde.gravemind == null)
        {
            return;
        }

        MobEffectInstance effectInstance = event.getEffectInstance();

        if(effectInstance == null)
        {
            return;
        }

        if(effectInstance.getEffect() instanceof IPotionExpireEffect iPotionExpireEffect)
        {
            iPotionExpireEffect.onPotionExpire(event);
        }
    }

    @SubscribeEvent
    public static void OnLivingDamageEvent(LivingDamageEvent event)
    {


        // Get Item being used to attack
        ItemStack itemStack = ItemStack.EMPTY;
        Entity damageSourceEntity = event.getSource().getEntity();
        LivingEntity targetEntity = event.getEntity();

        if(EntityAlgorithms.isSculkLivingEntity.test(targetEntity) && damageSourceEntity instanceof LivingEntity)
        {
            ModSavedData.getSaveData().addHostileToMemory((LivingEntity) damageSourceEntity);
        }


        if(damageSourceEntity instanceof LivingEntity attackingEntity)
        {
            itemStack = attackingEntity.getMainHandItem();
            if(!itemStack.getItem().equals(ModItems.SCULK_SWEEPER_SWORD.get()))
            {
               return;
            }

            if(EntityAlgorithms.isSculkLivingEntity.test(targetEntity))
            {
                itemStack.setDamageValue((int) Math.max(0, itemStack.getDamageValue() - event.getAmount()));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.player.level().isClientSide())
        {
            return;
        }

        if(event.player.tickCount % 20 == 0)
        {
            AdvancementUtil.advancementHandlingTick((ServerLevel) event.player.level());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(event.getEntity().level().isClientSide())
        {
            return;
        }

        if(SculkHorde.contributionHandler.isContributor((ServerPlayer) event.getEntity()) && !SculkHorde.contributionHandler.doesPlayerHaveContributionAdvancement((ServerPlayer) event.getEntity()))
        {
            AdvancementUtil.giveAdvancementToPlayer((ServerPlayer) event.getEntity(), ContributeTrigger.INSTANCE);
            SculkHorde.contributionHandler.givePlayerCoinOfContribution(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        if(PlayerProfileHandler.isPlayerActiveVessel(event.getEntity()))
        {
            MobEffectInstance effectInstance = new MobEffectInstance(ModMobEffects.SCULK_VESSEL.get(), Integer.MAX_VALUE);
            event.getEntity().addEffect(effectInstance);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevelEvent(EntityJoinLevelEvent event)
    {
        if(event.getLevel().isClientSide())
        {
            return;
        }

        if(event.getEntity() instanceof Mob mob)
        {
            if(!EntityAlgorithms.isLivingEntityExplicitDenyTarget(mob) && mob.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE))
            {
                mob.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(mob, LivingEntity.class, true, shouldEntitiesAttackTheSculkHorde));
            }
        }

        if(event.getEntity() instanceof Animal animal)
        {
            if(!EntityAlgorithms.isSculkLivingEntity.test(animal) && !EntityAlgorithms.isLivingEntityAllyToSculkHorde(animal))
            {
                animal.targetSelector.addGoal(0, new AvoidEntityGoal<LivingEntity>(animal, LivingEntity.class, 6.0F, 1.0F, 1.2F, shouldEntitiesAvoidTheSculkHorde));
            }
        }

        if(event.getEntity() instanceof Villager villager)
        {
            if(!EntityAlgorithms.isSculkLivingEntity.test(villager) && !EntityAlgorithms.isLivingEntityAllyToSculkHorde(villager))
            {
                villager.targetSelector.addGoal(0, new AvoidEntityGoal<LivingEntity>(villager, LivingEntity.class, 6.0F, 1.0F, 1.2F, shouldEntitiesAvoidTheSculkHorde));
            }
        }

    }

    public static Predicate<LivingEntity> shouldEntitiesAttackTheSculkHorde = (e) ->
    {
        return ModConfig.SERVER.should_all_other_mobs_attack_the_sculk_horde.get() && EntityAlgorithms.isSculkLivingEntity.test(e);
    };

    public static Predicate<LivingEntity> shouldEntitiesAvoidTheSculkHorde = (e) ->
    {
        return ModConfig.SERVER.should_animals_and_villagers_avoid_the_sculk_horde.get() && EntityAlgorithms.isSculkLivingEntity.test(e);
    };
}
