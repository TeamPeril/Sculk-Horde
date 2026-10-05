package com.github.sculkhorde.misc.contributions;

import com.github.sculkhorde.core.ModItems;
import com.github.sculkhorde.util.AdvancementUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.UUID;

public class ContributionHandler {

    public static final ArrayList<String> CONTRIBUTOR_UUIDS = new ArrayList<>();

    static {
        /// # Devs
        CONTRIBUTOR_UUIDS.add("548a65f1-5372-4e0b-8ebd-e337cd4895c8"); // Assassin_Mike
        CONTRIBUTOR_UUIDS.add("380df991-f603-344c-a090-369bad2a924a"); // Dev
        CONTRIBUTOR_UUIDS.add("1bb03aee-8460-497d-b3a6-b4707147c27d"); // SwagPotato345

        /// # YouTubers
        CONTRIBUTOR_UUIDS.add("fb0b51c0-bfa3-45a1-b914-afb8ab4380fd"); // KaratFeng

        //Keelios https://www.youtube.com/watch?v=4qqBZx3QTtc
        CONTRIBUTOR_UUIDS.add("015ada91-b10a-4afa-9846-8435c40706e6"); //Snowcon
        CONTRIBUTOR_UUIDS.add("19e779ac-d60a-47b9-bd96-a22ecfccdb7d"); //ItsArrzee
        CONTRIBUTOR_UUIDS.add("811c3d95-5a14-4b0b-ac62-650f9f041532"); //Keelios

        //FracturedWolf https://www.youtube.com/watch?v=dHTZ18-zxu4
        CONTRIBUTOR_UUIDS.add("53cbdba9-487b-4758-b81f-6b3f5ed411b6"); //FallenMurder2468

        //WilliamHelbent https://youtube.com/shorts/6ltcN--tk0Q?si=9P77sLjAXRqEqKul
        //video deleted, no username

        //Rotch Games - https://www.youtube.com/watch?v=rcd4DSjdZQ0
        CONTRIBUTOR_UUIDS.add("f0f346f5-ff1f-4bbc-b6f9-0fedb01d55d0"); //Rotch_Gwylt

        //Ghostlyy - https://www.youtube.com/watch?v=N3wXCr4cGl0
        CONTRIBUTOR_UUIDS.add("40ff4968-f491-4c2f-a75d-6f1ab35d662b"); // _Ghostlyy_

        //Quinity - https://www.youtube.com/watch?v=KYaRVx2US8E
        CONTRIBUTOR_UUIDS.add("386e1d98-11ad-44bd-b19c-0c067deaf276"); //Quinity101

        //ImFireyDude - https://www.youtube.com/shorts/u3n7SKaDT_g
        // Cannot find username

        //NutsAndBolts - https://www.youtube.com/watch?v=bdqNh8VOX3Y
        CONTRIBUTOR_UUIDS.add("ebe70f87-dd60-436b-a2ae-583ff4a56f02"); //NutsAndBoltsBro

        //MRTurtle - https://youtu.be/j9k8mzfYI_Y?si=o5SHmhx7l7-LzRA1
        CONTRIBUTOR_UUIDS.add("ef4452b9-5b3f-4401-ad96-46d39de1764a"); //TTurtl3e
        CONTRIBUTOR_UUIDS.add("82b65ff1-cf62-4790-969f-05dca4ee993c"); //88kyuta
        CONTRIBUTOR_UUIDS.add("c44132ce-a16e-4958-997e-a7648a868429"); //Kerfunk2

        CONTRIBUTOR_UUIDS.add("d3c887e1-f077-46b4-9466-98946da1b1fb"); // ForgeLabsSean
        CONTRIBUTOR_UUIDS.add("73f51138-562c-4de9-9fe7-5654da5d12e0"); // RobertGuy - https://www.youtube.com/watch?v=ELHIABGHdSc

        CONTRIBUTOR_UUIDS.add("a4cae489-8554-479d-8dfb-408366ef6781"); // Legundo - https://www.youtube.com/watch?v=pWyO39WPKH0

        CONTRIBUTOR_UUIDS.add("d9139d9c-e1e7-453e-b296-5f7615590fe1"); // Dongerstein
        CONTRIBUTOR_UUIDS.add("aa31ba7c-590d-419c-b14e-b27f91c05547"); // Speghettleg
        CONTRIBUTOR_UUIDS.add("7df179bd-71ca-4af5-86b5-0e49cffa80b4"); // 0dna

        CONTRIBUTOR_UUIDS.add("e431b73f-c6a0-4a89-aef4-139fb92bc2e2"); // SirColor - https://www.youtube.com/watch?v=Ke4zdWzbqOk

        CONTRIBUTOR_UUIDS.add("732d984b-2144-4bf4-b465-7f494514243c"); // EqlipseTV

        CONTRIBUTOR_UUIDS.add("b2b8fa9a-1466-49e1-9b1b-b3ca713606de"); // Jakethegreat_74

        CONTRIBUTOR_UUIDS.add("10238e01-d616-4b5a-bc98-7a2ff5ddba74"); // Lynixity
        CONTRIBUTOR_UUIDS.add("7ffd2899-a39c-4cae-a082-25dadfdf9629"); // Kiply
        CONTRIBUTOR_UUIDS.add("b20bbad5-0e54-4f2f-abd1-c51fd0f0a984"); // RagePlaysGames

        CONTRIBUTOR_UUIDS.add("665ad10f-5548-4985-986b-b642732ddce0"); // Kaupenjoe

        CONTRIBUTOR_UUIDS.add("218db97a-2b9e-4101-b897-96e068dc064f"); // TheDestroyer6928
        CONTRIBUTOR_UUIDS.add("9abf3dbc-b683-4e05-bf6c-3059605e1633"); // Red_2101

        //"Adding 16 Dwellers With Minecraft's Sculk Horde" - https://www.youtube.com/watch?v=diAgJcSmleQ
        CONTRIBUTOR_UUIDS.add("111247de-03ca-4a07-8af7-54a8e5ff7ce7"); // Ivan_the_Moron
        CONTRIBUTOR_UUIDS.add("e0f1336a-e6a5-43d4-8ac9-33d4c40a5f8f"); // Codekid_
        CONTRIBUTOR_UUIDS.add("225b2681-7298-4b88-91e7-5fba916b7f46"); // Jamsteobro

        CONTRIBUTOR_UUIDS.add("473e2403-2eff-4e67-94d9-31a78156980a"); // mythicninja - https://www.youtube.com/watch?v=V1euV4mwMFI

        //Project S, modded survival Minecraft SMP https://www.youtube.com/watch?v=f25MdHjaZ5w
        CONTRIBUTOR_UUIDS.add("94bd16b1-0595-478f-a279-3ae4ef105795"); // immortalhipster
        CONTRIBUTOR_UUIDS.add("0b759422-acd5-467e-b134-b92e5383ba68"); // Wolfie_Luke
        CONTRIBUTOR_UUIDS.add("24447965-b4d4-4393-bba3-9cafd9595521"); // SillyLinn
        CONTRIBUTOR_UUIDS.add("bd95a4ee-23b1-4185-975b-a6d01484123e"); // Samuelis
        CONTRIBUTOR_UUIDS.add("1818d574-13cc-471d-a2b7-4ec63caf73e9"); // TheRedRobber
        CONTRIBUTOR_UUIDS.add("4712d5b7-e286-429a-952a-9123d9520937"); // Wabadoodel
        CONTRIBUTOR_UUIDS.add("770c6153-7715-4271-942f-ed8119d70f49"); // Poke_Snivy
        CONTRIBUTOR_UUIDS.add("ea21dad6-f7e6-4c4b-8fa7-020c767cbf38"); // CrazyNatureKitty
        CONTRIBUTOR_UUIDS.add("3627429d-6f80-48f2-96a8-a4413c5faff8"); // Star_Aiden

        //Moth Plays 1.20.1 Parasites Zombies and Colonies https://www.youtube.com/watch?v=az1hCYSh9WY
        CONTRIBUTOR_UUIDS.add("63d1cfb6-247d-490d-9b1f-5af8f1939fbb"); // Wendifoe

        // Sculk Horde Survival modpack with extra mods with friends reboot https://www.youtube.com/watch?v=-Mb2f1TIq1g
        CONTRIBUTOR_UUIDS.add("3d4f88ef-1dcb-46c7-aa1d-77c869a7d4b2"); // sophienix

        // Bunch of Sculk Horde vids https://www.youtube.com/watch?v=cIhiy7i2h54
        CONTRIBUTOR_UUIDS.add("2c9588bb-6288-4734-a49f-cb022aba8ad7"); // TGWalker

        //2 Idiots Fight The Sculk Horde https://www.youtube.com/watch?v=h-EbhQyls3Q
        CONTRIBUTOR_UUIDS.add("c4e2261a-5b31-44aa-89f1-39debfcfd136"); // DR_28
        CONTRIBUTOR_UUIDS.add("4003520d-5537-41f8-9efd-63fd77df32f3"); // Kevoonio

        // Preparing for the Sculk Horde! Unnamed SMP https://www.youtube.com/watch?v=PCgLKJJNwuM
        CONTRIBUTOR_UUIDS.add("453879c4-b193-4e38-9f04-d1560cd99fe7"); // jearldster

        //Surviving 100 Days during a Sculk Outbreak in Hardcore Minecraft | Chaste and Pure edition https://www.youtube.com/watch?v=Gx2joIIwMO0
        CONTRIBUTOR_UUIDS.add("5419a1c9-0aa8-45f5-8217-9be47140492c"); // sneve

         /// # Contributors & Community
        CONTRIBUTOR_UUIDS.add("f0b9f831-5540-47c8-b50b-64fe4ee40341"); // AnukWarrior
        CONTRIBUTOR_UUIDS.add("838dd85f-95b9-4a9d-a785-7694e8c7c8b1"); // Abandoned_Cat87
        CONTRIBUTOR_UUIDS.add("046ae4a5-ec4e-4bf3-a054-f72a67147ec6"); // AbsoluteKun
        CONTRIBUTOR_UUIDS.add("719f4b13-4fe1-409e-9109-5eab941ed453"); // AME_Player_5555
        CONTRIBUTOR_UUIDS.add("3d4634bc-f0be-4989-81e8-b8cbe4cac02e"); // Aphrodite412
        CONTRIBUTOR_UUIDS.add("265e4c2d-420d-43d6-9feb-561a3e83aa8e"); // boss9686
        CONTRIBUTOR_UUIDS.add("016472d7-68f8-4b2e-b344-904f20ea4f2c"); // citizern
        CONTRIBUTOR_UUIDS.add("f0df41e0-859b-4f2f-b08e-0fe7698a3d64"); // DerpyBuddy
        CONTRIBUTOR_UUIDS.add("767901be-f9f0-4ed8-908a-896550bc930c"); // DreadedGaming
        CONTRIBUTOR_UUIDS.add("f9d2df29-35c5-4fc6-a2f0-aa2576d8135a"); // Exca1ybur
        CONTRIBUTOR_UUIDS.add("8c89a0d3-3271-459d-a8c1-a9d34d53365b"); // FunkyMonk164
        CONTRIBUTOR_UUIDS.add("08dc5aaa-e98b-4c46-b3b8-fd6c32acc76d"); // Goggalcon6
        CONTRIBUTOR_UUIDS.add("bdc4f0d8-486f-408d-ae34-e57c7b98786f"); // Herobrine_42
        CONTRIBUTOR_UUIDS.add("838de04d-f661-497d-93fc-dcaceb226a42"); // Jason_Lamina
        CONTRIBUTOR_UUIDS.add("c00ac6f3-c4bf-4850-8706-8b1d66b0168a"); // Losgann2
        CONTRIBUTOR_UUIDS.add("f261d52d-9fb6-4141-8139-7308486c3ca6"); // Lunasafaro
        CONTRIBUTOR_UUIDS.add("310b94fd-7293-4a71-853f-53ebabb22549"); // monsterboogs
        CONTRIBUTOR_UUIDS.add("a6f2251a-7f26-4305-a41e-0ae87fc617e8"); // motaywo
        CONTRIBUTOR_UUIDS.add("29aa413b-d714-46f1-a3f5-68b9c67a4923"); // Ninjaguy169
        CONTRIBUTOR_UUIDS.add("0aa268c8-0c0e-46df-98e5-2dc83bf543cc"); // PiggyDragons
        CONTRIBUTOR_UUIDS.add("a325d18a-4eb8-4919-8b59-860f901f1f05"); // Polarice3
        CONTRIBUTOR_UUIDS.add("19889b43-2a39-462b-ad34-237007f614f0"); // pvz_fan2
        CONTRIBUTOR_UUIDS.add("6b4e8fc2-fa24-4d9e-a911-25342e0e1315"); // QuartzKor
        CONTRIBUTOR_UUIDS.add("6ae6f1a2-8bef-4864-99f0-77c1bf64cb28"); // Sire_AwfulThe1st
        CONTRIBUTOR_UUIDS.add("db5723f4-d9c9-4364-ab74-d1f630a4ce65"); // SkyelanderZero
        CONTRIBUTOR_UUIDS.add("764fb10e-0f58-4429-a9fa-7c1aa1313678"); // TheCaramelGuy
        CONTRIBUTOR_UUIDS.add("fe89656b-14dd-471b-8fcd-ace66ce86345"); // therealglados
        CONTRIBUTOR_UUIDS.add("9637865a-70d3-488f-87a3-60e67ae58dc5"); // ToastedLink
        CONTRIBUTOR_UUIDS.add("684385ec-9d20-4ccd-b9fe-c9b45483b138"); // UnanimousVoid
        CONTRIBUTOR_UUIDS.add("989d88f0-eb7e-436f-b0da-d72b97fa4079"); // Sketano
        CONTRIBUTOR_UUIDS.add("ce08a8e1-eed6-4240-8fef-655b28f81cde"); // Kierbo05
        CONTRIBUTOR_UUIDS.add("4ccea4c9-0ada-4225-8739-eda41cb4df25"); // Masterofk3gs

        CONTRIBUTOR_UUIDS.add("6e8ccf60-1799-4f98-aa2a-964015339624"); // dwalkrun
        CONTRIBUTOR_UUIDS.add("9c71f1b8-c1ab-4632-a3af-1530539a1b7b"); // MrBallYT
        CONTRIBUTOR_UUIDS.add("4a66cddc-c4b5-45a4-8c02-8c699017b6cd"); // BLUEKOZ
        CONTRIBUTOR_UUIDS.add("b3d39f25-8e82-4a02-9970-739b765b3027"); // Royito123170
        CONTRIBUTOR_UUIDS.add("08dc5aaa-e98b-4c46-b3b8-fd6c32acc76d"); // Goggalcon6
        CONTRIBUTOR_UUIDS.add("8e1986b0-148a-43f8-b21d-5cefe1ca74d4"); // xoom7
        CONTRIBUTOR_UUIDS.add("405c4df2-1162-4a41-9540-9ab9f0e4e522"); // Bioscar_YT
        CONTRIBUTOR_UUIDS.add("f4789bd4-84ef-4ff8-b5a6-e0d41033ea02"); // joshpd8318
        CONTRIBUTOR_UUIDS.add("667175bb-2769-4b97-b91e-efb4a2e0ec9c"); // aarter
        CONTRIBUTOR_UUIDS.add("5d815bf0-b728-4f59-9444-4d87901d0efe"); // Dancy_TWDS
        CONTRIBUTOR_UUIDS.add("2a99ab5b-7be3-4de3-8db4-8726fbd3b688"); // petrolpark
        // "theactualrealglados" was here on original list but is too long to be a username
        CONTRIBUTOR_UUIDS.add("a6756fbc-3f5f-4cd3-b8b0-42317b486d14"); // MelonGodKing
        CONTRIBUTOR_UUIDS.add("a2655812-6ac5-4d07-8e26-5e92ae04f20a"); // AeroHearts
        CONTRIBUTOR_UUIDS.add("e1daf281-8cd1-4280-94d1-9e53ea8e728c"); // BurningSock
        CONTRIBUTOR_UUIDS.add("9e8b5507-ce7a-421e-a3c5-c6f5059bce43"); // Kerunith
        CONTRIBUTOR_UUIDS.add("d7b11fe3-afb8-42d6-bfb4-caec576aaef3"); // EhnenehMari
    }

    private final ArrayList<UUID> listOfContributors = new ArrayList<>();

    public ArrayList<UUID> getListOfContributors() {
        return listOfContributors;
    }

    public ContributionHandler()
    {
        for (String contributorUUID : CONTRIBUTOR_UUIDS) {
            addContributor(contributorUUID);
        }
    }

    public void addContributor(String uuid)
    {
        UUID contributorUUID = UUID.fromString(uuid);
        if(!getListOfContributors().contains(contributorUUID))
        {
            getListOfContributors().add(contributorUUID);
        }
    }

    public boolean doesPlayerHaveContributionAdvancement(ServerPlayer player)
    {
        return AdvancementUtil.isAdvancementCompleted(player, ResourceLocation.parse("sculkhorde:contribute"));
    }

    public boolean isContributor(ServerPlayer player)
    {
        return getListOfContributors().contains(player.getUUID());
    }

    public void givePlayerCoinOfContribution(Player player)
    {
        ItemStack coin = new ItemStack(ModItems.COIN_OF_CONTRIBUTION.get());
        player.addItem(coin);
    }
}
