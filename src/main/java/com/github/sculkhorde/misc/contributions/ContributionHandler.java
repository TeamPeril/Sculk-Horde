package com.github.sculkhorde.misc.contributions;

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
        addContributor("SwagPotato345");

        addContributor("KaratFeng");

        addContributor("ForgeLabsSean");
        addContributor("RobertGuy"); //https://www.youtube.com/watch?v=ELHIABGHdSc

        addContributor("Legundo"); //https://www.youtube.com/watch?v=pWyO39WPKH0

        addContributor("Dongerstein");
        addContributor("Spaghettleg");
        addContributor("0dna");

        addContributor("SirColor"); //https://www.youtube.com/watch?v=Ke4zdWzbqOk

        addContributor("EqlipseTV");

        addContributor("Jakethegreat_74");

        addContributor("Lynixity");
        addContributor("Kiply");
        addContributor("RagePlaysGames");

        addContributor("Kaupenjoe");

        addContributor("TheDestroyer6928");
        addContributor("Red_2101");

        //"Adding 16 Dwellers With Minecraft's Sculk Horde" - https://www.youtube.com/watch?v=diAgJcSmleQ
        addContributor("Ivan_the_Moron");
        addContributor("Codekid_");
        addContributor("Jamsteobro");

        addContributor("MythicNinja"); //https://www.youtube.com/watch?v=V1euV4mwMFI&t=828s

        //Project S, modded survival Minecraft SMP https://www.youtube.com/watch?v=f25MdHjaZ5w
        addContributor("immortalhipster");
        addContributor("Wolfie_Luke");
        addContributor("SillyLinn");
        addContributor("Samuelis");
        addContributor("TheRedRobber");
        addContributor("Wabadoodel");
        addContributor("Poke_Snivy");
        addContributor("CrazyNatureKitty");
        addContributor("Samuelis");
        addContributor("Star_Aiden");

        //Moth Plays 1.20.1 Parasites Zombies and Colonies https://www.youtube.com/watch?v=az1hCYSh9WY&t=437s
        addContributor("Wendifoe");

        // Sculk Horde Survival modpack with extra mods with friends reboot https://www.youtube.com/watch?v=-Mb2f1TIq1g
        addContributor("Sophienix");

        // Bunch of Sculk Horde vids https://www.youtube.com/watch?v=cIhiy7i2h54
        addContributor("TGWalker");

        //2 Idiots Fight The Sculk Horde https://www.youtube.com/watch?v=h-EbhQyls3Q
        addContributor("DR_28");
        addContributor("Kevoonio");

        // Preparing for the Sculk Horde! Unnamed SMP https://www.youtube.com/watch?v=PCgLKJJNwuM
        addContributor("jearldster");

        //Surviving 100 Days during a Sculk Outbreak in Hardcore Minecraft | Chaste and Pure edition https://www.youtube.com/watch?v=Gx2joIIwMO0
        addContributor("Sneve");

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
        addContributor("BurningSock");
        addContributor("Kerunith");
        addContributor("EhnenehMari");
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
