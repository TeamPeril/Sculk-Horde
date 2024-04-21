package com.github.sculkhorde.misc;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;

public class ModColaborationHelper {


    // https://www.curseforge.com/minecraft/mc-mods/from-another-world

    private static TagKey<EntityType<?>> fromAnotherWorldEntityTagKey = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("fromanotherworld:things"));

    public static boolean isFromAnotherWorldLoaded()
    {
        return ModList.get().isLoaded("fromanotherworld");
    }

    public static boolean isThisAFromAnotherWorldEntity(LivingEntity entity)
    {
        if(!isFromAnotherWorldLoaded())
        {
            return false;
        }

        return entity.getType().is(fromAnotherWorldEntityTagKey);
    }

    // https://www.curseforge.com/minecraft/mc-mods/fungal-infection-spore
    public static boolean isSporeLoaded()
    {
        return ModList.get().isLoaded("spore");
    }

    private static TagKey<EntityType<?>> sporeEntityTagKey = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("spore:fungus_entities"));

    public static boolean isThisASporeEntity(LivingEntity entity)
    {
        if(!isSporeLoaded())
        {
            return false;
        }

        boolean isSporeEntity = entity.getType().is(sporeEntityTagKey);
        return isSporeEntity;
    }

    public static boolean isArsNouveauLoaded()
    {
        return ModList.get().isLoaded("ars_nouveau");
    }

    public static boolean isThisAnArsNouveauBlackListEntity(LivingEntity entity)
    {
        if(!isArsNouveauLoaded())
        {
            return false;
        }

        ResourceLocation targetEntityResourceLocation = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        String entityNameSpace = targetEntityResourceLocation.toString();
        if(entityNameSpace.equals("ars_nouveau:drygmy"))
        {
            return true;
        }
        else if(entityNameSpace.equals("ars_nouveau:whirlisprig"))
        {
            return true;
        }
        else if(entityNameSpace.equals("ars_nouveau:starbuncle"))
        {
            return true;
        }

        return false;
    }
}
