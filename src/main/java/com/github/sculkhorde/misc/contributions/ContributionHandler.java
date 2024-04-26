package com.github.sculkhorde.misc.contributions;

import com.github.sculkhorde.common.advancement.ContributeTrigger;
import com.github.sculkhorde.core.ModItems;
import com.github.sculkhorde.util.AdvancementUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;

public class ContributionHandler {

    HashMap<String, Boolean> listOfContributors = new HashMap<>();

    public HashMap<String, Boolean> getListOfContributors() {
        return listOfContributors;
    }

    public ContributionHandler()
    {
        addContributor("Assassin_Mike");
        addContributor("Dev");
        addContributor("Sly3501");
        addContributor("SwagPotato345");

        addContributor("KaratFeng");

        addContributor("ForgeLabsSean");

        addContributor("Dongerstein");
        addContributor("Spaghettleg");
        addContributor("0dna");

        addContributor("SirColor");

        addContributor("EqlipseTV");

        addContributor("Jakethegreat_74");

        addContributor("Lynixity");
        addContributor("Kiply");
        addContributor("RagePlaysGames");

        addContributor("Abandoned_Cat87");
        addContributor("AbsoluteKun");
        addContributor("AME_Player_5555");
        addContributor("Aphrodite412");
        addContributor("boss9686");
        addContributor("citizern");
        addContributor("DerpyBuddy");
        addContributor("DreadedGaming");
        addContributor("Exca1ybur");
        addContributor("FunkyMonk127");
        addContributor("Goggalcon6");
        addContributor("Herobrine_42");
        addContributor("Jason_Lamina");
        addContributor("Losgann2");
        addContributor("Lunasafaro");
        addContributor("monsterboogs");
        addContributor("motaywo");
        addContributor("Ninjaguy169");
        addContributor("PiggyDragons");
        addContributor("Polarice3");
        addContributor("pvz_fan1");
        addContributor("QuartzKor");
        addContributor("Sire_AwfulThe1st");
        addContributor("SkyelanderZero");
        addContributor("TheCaramelGuy");
        addContributor("therealglados");
        addContributor("ToastedLink");
        addContributor("UnanimousVoid");
        addContributor("_Sketano");
        addContributor("Kierbo05");
        addContributor("MasterofK3gs");

        addContributor("dwalkrun");
        addContributor("MrBall2748");
        addContributor("BLUEKOZ");
        addContributor("Royito123170");
        addContributor("Goggalcon6");
        addContributor("xoom7");
        addContributor("FunkyMonk127");
        addContributor("Bioscar_YT");
        addContributor("joshpd8318");
        addContributor("aarter");
        addContributor("EpochIH");
        addContributor("petrolpark");
        addContributor("theactualrealglados");
        addContributor("MelonGodKing");
        addContributor("AeroHearts");
    }

    public void addContributor(String name)
    {
        getListOfContributors().put(name.toLowerCase(),true);
    }

    public boolean doesPlayerHaveContributionAdvancement(ServerPlayer player)
    {
        return AdvancementUtil.isAdvancementCompleted(player, new ResourceLocation("sculkhorde:contribute"));
    }

    public boolean isContributor(ServerPlayer player)
    {
        String scoreboardName = player.getScoreboardName().toString().toLowerCase();

        if(getListOfContributors().get(scoreboardName) != null)
        {
            return true;
        }

        return false;
    }

    public void givePlayerCoinOfContribution(Player player)
    {
        ItemStack coin = new ItemStack(ModItems.COIN_OF_CONTRIBUTION.get());
        player.addItem(coin);
    }
}
