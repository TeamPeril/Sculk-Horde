package com.github.sculkhorde.misc;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.ModList;

public class ModColaborationHelper {

    protected static String extractModId(Entity entity) {
        return extractModId(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
    }

    protected static String extractModId(String entityNamespace) {
        if (entityNamespace.contains(":"))
        {
            return entityNamespace.split(":")[0];
        }
        else
        {
            throw new IllegalArgumentException("Invalid entity namespace format. Expected format 'mod_id:entity_name'.");
        }
    }


    // https://www.curseforge.com/minecraft/mc-mods/from-another-world
    public static String FROM_ANOTHER_WORLD_ID = "fromanotherworld";
    private static TagKey<EntityType<?>> FROM_ANOTHER_WORLD_TAG_KEY = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse(FROM_ANOTHER_WORLD_ID + ":things"));

    public static boolean isFromAnotherWorldLoaded()
    {
        return ModList.get().isLoaded(FROM_ANOTHER_WORLD_ID);
    }

    public static boolean doesEntityBelongToFromAnotherWorldMod(LivingEntity entity)
    {
        if(!isFromAnotherWorldLoaded())
        {
            return false;
        }

        return entity.getType().is(FROM_ANOTHER_WORLD_TAG_KEY);
    }



    // https://www.curseforge.com/minecraft/mc-mods/fungal-infection-spore
    public static String SPORE_ID = "spore";
    public static boolean isSporeLoaded()
    {
        return ModList.get().isLoaded(SPORE_ID);
    }

    private static TagKey<EntityType<?>> SPORE_TAG_KEY = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse(SPORE_ID + ":fungus_entities"));

    public static boolean doesEntityBelongToSporeMod(LivingEntity entity)
    {
        if(!isSporeLoaded())
        {
            return false;
        }

        return entity.getType().is(SPORE_TAG_KEY);
    }


    /// #### Depper and Darker ####
    public static String DEEPER_AND_DARKER = "deeper_and_darker";
    public static boolean isDeeperAndDarkerLoaded()
    {
        return ModList.get().isLoaded(DEEPER_AND_DARKER);
    }

    public static boolean doesEntityBelongToDeeperAndDarkerMod(LivingEntity entity)
    {
        if(!isDeeperAndDarkerLoaded())
        {
            return false;
        }

        String entityModID = extractModId(entity);
        return entityModID.equals(DEEPER_AND_DARKER);
    }



    /// # Dawn of the Flood
    public static String DAWN_OF_THE_FLOOD_ID = "dotf";
    public static boolean isDawnOfTheFloodLoaded()
    {
        return ModList.get().isLoaded(DAWN_OF_THE_FLOOD_ID);
    }

    public static boolean doesEntityBelongToDawnOfTheFloodMod(LivingEntity entity)
    {
        if(!isDawnOfTheFloodLoaded())
        {
            return false;
        }

        String entityModID = extractModId(entity);
        return entityModID.equals(DAWN_OF_THE_FLOOD_ID);
    }



    /// #### Another Dimension Invasion ####
    public static String ANOTHER_DIMENSION_INVASION_ID = "invasion";
    public static boolean isAnotherDimensionInvasionLoaded()
    {
        return ModList.get().isLoaded(ANOTHER_DIMENSION_INVASION_ID);
    }

    public static boolean doesEntityBelongToAnotherDimensionInvasionMod(LivingEntity entity)
    {
        if(!isAnotherDimensionInvasionLoaded())
        {
            return false;
        }

        String entityModID = extractModId(entity);
        return entityModID.equals(ANOTHER_DIMENSION_INVASION_ID);
    }



    /// #### Swarm Infection ####
    public static String SWARM_INFECTION_ID = "swarm_infection";
    public static boolean isSwarmInfectionLoaded()
    {
        return ModList.get().isLoaded(SWARM_INFECTION_ID);
    }

    public static boolean doesEntityBelongToSwarmInfectionMod(LivingEntity entity)
    {
        if(!isSwarmInfectionLoaded())
        {
            return false;
        }

        String entityModID = extractModId(entity);
        return entityModID.equals(SWARM_INFECTION_ID);
    }



    /// #### The Flesh That Hates ####
    public static String FLESH_THAT_HATES_ID = "the_flesh_that_hates";
    private static TagKey<EntityType<?>> FLESH_THAT_HATES_TAG_KEY = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse(FLESH_THAT_HATES_ID + ":fleshy_entities"));
    public static boolean isTheFleshThatHatesLoaded()
    {
        return ModList.get().isLoaded(FLESH_THAT_HATES_ID);
    }

    public static boolean doesEntityBelongToTheFleshThatHatesMod(LivingEntity entity)
    {
        if(!isTheFleshThatHatesLoaded())
        {
            return false;
        }

        return entity.getType().is(FLESH_THAT_HATES_TAG_KEY);
    }



    /// #### Withering Away Reborn ####
    public static String WITHERING_AWAY_REBORN_ID = "withering_away_reborn";
    public static boolean isWitheringAwayRebornLoaded()
    {
        return ModList.get().isLoaded(WITHERING_AWAY_REBORN_ID);
    }

    public static boolean doesEntityBelongToWitheringAwayRebornMod(LivingEntity entity)
    {
        if(!isWitheringAwayRebornLoaded())
        {
            return false;
        }

        String entityModID = extractModId(entity);
        return entityModID.equals(WITHERING_AWAY_REBORN_ID);
    }



    /// #### Abominations Infection ####
    public static String ABOMINATIONS_INFECTION_ID = "abominations_infection";
    public static boolean isAbominationsInfectionLoaded()
    {
        return ModList.get().isLoaded(ABOMINATIONS_INFECTION_ID);
    }

    public static boolean doesEntityBelongToAbominationsInfectionMod(LivingEntity entity)
    {
        if(!isAbominationsInfectionLoaded())
        {
            return false;
        }

        String entityModID = extractModId(entity);
        return entityModID.equals(ABOMINATIONS_INFECTION_ID);
    }



    /// #### Prion Infection ####
    public static String PRION_INFECTION_ID = "prionmod";
    public static boolean isPrionInfectionLoaded()
    {
        return ModList.get().isLoaded(PRION_INFECTION_ID);
    }

    public static boolean doesEntityBelongToPrionInfectionMod(LivingEntity entity)
    {
        if(!isPrionInfectionLoaded())
        {
            return false;
        }

        String entityModID = extractModId(entity);
        return entityModID.equals(PRION_INFECTION_ID);
    }



    /// #### Bulbus Infection ####
    public static String BULBUS_INFECTION_ID = "bulbus";
    public static boolean isBulbusLoaded()
    {
        return ModList.get().isLoaded(BULBUS_INFECTION_ID);
    }

    public static boolean doesEntityBelongToBulbusMod(LivingEntity entity)
    {
        if(!isBulbusLoaded())
        {
            return false;
        }

        String entityModID = extractModId(entity);
        return entityModID.equals(BULBUS_INFECTION_ID);
    }



    /// #### Entomophobia ####
    public static String ENTOMOPHOBIA_ID = "entomophobia";
    public static boolean isEntomophobiaLoaded()
    {
        return ModList.get().isLoaded(ENTOMOPHOBIA_ID);
    }

    public static boolean doesEntityBelongToEntomophobiaMod(LivingEntity entity)
    {
        if(!isEntomophobiaLoaded())
        {
            return false;
        }

        String entityModID = extractModId(entity);
        return entityModID.equals(ENTOMOPHOBIA_ID);
    }

    /// #### Complete Distortion Infection ####
    public static String COMPLETE_DISTORTION_INFECTION_ID = "complete_distortion_reborn";
    public static boolean isCompleteDistortionInfectionLoaded()
    {
        return ModList.get().isLoaded(COMPLETE_DISTORTION_INFECTION_ID);
    }

    public static boolean doesEntityBelongToCompleteDistortionInfectionMod(LivingEntity entity)
    {
        if(!isCompleteDistortionInfectionLoaded())
        {
            return false;
        }

        String entityModID = extractModId(entity);
        return entityModID.equals(COMPLETE_DISTORTION_INFECTION_ID);
    }

    /// #### Pharyriosis Parasite Infection ####
    public static String PHAYRIOSIS_PARASITE_INFECTION_ID = "phayriosis";
    public static boolean isPharyriosisParasiteInfectionLoaded()
    {
        return ModList.get().isLoaded(PHAYRIOSIS_PARASITE_INFECTION_ID);
    }

    public static boolean doesEntityBelongToPharyriosisParasiteInfectionMod(LivingEntity entity)
    {
        if(!isPharyriosisParasiteInfectionLoaded())
        {
            return false;
        }

        String entityModID = extractModId(entity);
        return entityModID.equals(PHAYRIOSIS_PARASITE_INFECTION_ID);
    }



    /// #### Mi Alliance ####
    public static String MI_ALLIANCE_ID = "mialliance";
    public static boolean isMiAllianceLoaded()
    {
        return ModList.get().isLoaded(MI_ALLIANCE_ID);
    }

    public static boolean doesEntityBelongToMIAllianceMod(LivingEntity entity)
    {
        if(!isMiAllianceLoaded())
        {
            return false;
        }

        String entityModID = extractModId(entity);
        return entityModID.equals(MI_ALLIANCE_ID);
    }

    /// #### Scape and Run Parasites ####
    public static String SCAPE_AND_RUN_PARASITES_ID = "srparasites";
    public static boolean isScapeAndRunParasitesLoaded()
    {
        return ModList.get().isLoaded(SCAPE_AND_RUN_PARASITES_ID);
    }

    public static boolean doesEntityBelongToScapeAndRunParasitesMod(LivingEntity entity)
    {
        if(!isScapeAndRunParasitesLoaded())
        {
            return false;
        }

        String entityModID = extractModId(entity);
        return entityModID.equals(SCAPE_AND_RUN_PARASITES_ID);
    }


    /// #### ArsNouveau ####

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

    /// # The Dulling
    public static String DULLING_ID = "thedulling";
    public static boolean isDullingLoaded()
    {
        return ModList.get().isLoaded(DULLING_ID);
    }

    private static TagKey<EntityType<?>> DULL_TAG_KEY = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse("minecraft" + ":dull"));

    public static boolean doesEntityBelongToDullingMod(LivingEntity entity)
    {
        if(!isDullingLoaded())
        {
            return false;
        }

        return entity.getType().is(DULL_TAG_KEY);
    }



}
