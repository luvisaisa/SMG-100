package com.completionist.model;

import java.util.List;

// builds the whole super mario galaxy game structure
// all 121 stars organized into domes and galaxies
public class GameFactory {

    // creates the full SMG game with everything
    public static Game createSuperMarioGalaxy() {
        // ===== TUTORIAL / GATEWAY =====
        Galaxy gateway = createGatewayGalaxy();

        Dome tutorial = new Dome("tutorial", "Gateway",
            List.of(gateway));
        // gateway is always unlocked (starting area)

        // ===== TERRACE DOME =====
        Galaxy goodEgg = createGoodEggGalaxy();
        Galaxy honeyhive = createHoneyhiveGalaxy();
        Galaxy loopdeeloop = createLoopdeeloopGalaxy();
        Galaxy flipswitch = createFlipswitchGalaxy();
        Galaxy bowserJrRobot = createBowserJrRobotReactorGalaxy();
        Galaxy sweetSweet = createSweetSweetGalaxy();

        Dome terrace = new Dome("terrace", "Terrace",
            List.of(goodEgg, honeyhive, loopdeeloop, flipswitch, bowserJrRobot, sweetSweet));
        // terrace is always unlocked (first dome)

        // ===== FOUNTAIN DOME =====
        Galaxy spaceJunk = createSpaceJunkGalaxy();
        Galaxy battlerock = createBattlerockGalaxy();
        Galaxy rollingGreen = createRollingGreenGalaxy();
        Galaxy hurryScurry = createHurryScurryGalaxy();
        Galaxy bowserStar = createBowserStarReactorGalaxy();
        Galaxy slingPod = createSlingPodGalaxy();

        Dome fountain = new Dome("fountain", "Fountain",
            List.of(spaceJunk, battlerock, rollingGreen, hurryScurry, bowserStar, slingPod));
        fountain.setUnlockCondition(new TotalStarsCondition(7));

        // ===== KITCHEN DOME =====
        Galaxy beachBowl = createBeachBowlGalaxy();
        Galaxy ghostly = createGhostlyGalaxy();
        Galaxy bubbleBreeze = createBubbleBreezeGalaxy();
        Galaxy buoyBase = createBuoyBaseGalaxy();
        Galaxy bowserJrAirship = createBowserJrAirshipArmadaGalaxy();
        Galaxy dripDrop = createDripDropGalaxy();

        Dome kitchen = new Dome("kitchen", "Kitchen",
            List.of(beachBowl, ghostly, bubbleBreeze, buoyBase, bowserJrAirship, dripDrop));
        kitchen.setUnlockCondition(new TotalStarsCondition(18));

        // ===== BEDROOM DOME =====
        Galaxy gustyGarden = createGustyGardenGalaxy();
        Galaxy freezeflame = createFreezeflameGalaxy();
        Galaxy dustyDune = createDustyDuneGalaxy();
        Galaxy honeyclimb = createHoneyclimbGalaxy();
        Galaxy bowserDarkMatter = createBowserDarkMatterPlantGalaxy();
        Galaxy bigmouth = createBigmouthGalaxy();

        Dome bedroom = new Dome("bedroom", "Bedroom",
            List.of(gustyGarden, freezeflame, dustyDune, honeyclimb, bowserDarkMatter, bigmouth));
        bedroom.setUnlockCondition(new TotalStarsCondition(33));

        // ===== ENGINE ROOM DOME =====
        Galaxy goldLeaf = createGoldLeafGalaxy();
        Galaxy seaSlide = createSeaSlideGalaxy();
        Galaxy toyTime = createToyTimeGalaxy();
        Galaxy bonefin = createBonefinGalaxy();
        Galaxy bowserJrLava = createBowserJrLavaReactorGalaxy();
        Galaxy sandSpiral = createSandSpiralGalaxy();

        Dome engineRoom = new Dome("engine-room", "Engine Room",
            List.of(goldLeaf, seaSlide, toyTime, bonefin, bowserJrLava, sandSpiral));
        engineRoom.setUnlockCondition(new TotalStarsCondition(45));

        // ===== GARDEN DOME =====
        Galaxy deepDark = createDeepDarkGalaxy();
        Galaxy dreadnought = createDreadnoughtGalaxy();
        Galaxy meltyMolten = createMeltyMoltenGalaxy();
        Galaxy matterSplatter = createMatterSplatterGalaxy();
        Galaxy snowCap = createSnowCapGalaxy();
        Galaxy boosBoneyard = createBoosBoneyardGalaxy();
        Galaxy bowserGalaxyReactor = createBowserGalaxyReactorGalaxy();

        Dome garden = new Dome("garden", "Garden",
            List.of(deepDark, dreadnought, meltyMolten, matterSplatter, snowCap, boosBoneyard, bowserGalaxyReactor));
        garden.setUnlockCondition(new TotalStarsCondition(50));

        // ===== PLANET OF TRIALS =====
        Galaxy rollingGizmo = createRollingGizmoGalaxy();
        Galaxy bubbleBlast = createBubbleBlastGalaxy();
        Galaxy loopdeeswoop = createLoopdeeswoopGalaxy();

        Dome planetOfTrials = new Dome("planet-of-trials", "Planet of Trials",
            List.of(rollingGizmo, bubbleBlast, loopdeeswoop));
        // unlocks after getting all 3 green stars
        planetOfTrials.setUnlockCondition(new GreenStarsUnlockCondition());

        // ===== GRAND FINALE =====
        Galaxy grandFinale = createGrandFinaleGalaxy();

        Dome grandFinaleDome = new Dome("grand-finale", "Grand Finale Galaxy",
            List.of(grandFinale));
        grandFinaleDome.setUnlockCondition(new TotalStarsCondition(60));

        // Super Mario Galaxy game
        return new Game("super-mario-galaxy", "Super Mario Galaxy",
            List.of(tutorial, terrace, fountain, kitchen, bedroom, engineRoom, garden, planetOfTrials, grandFinaleDome));
    }

    // ===== TUTORIAL / GATEWAY GALAXIES =====

    private static Galaxy createGatewayGalaxy() {
        return new Galaxy("gateway", "Gateway Galaxy", List.of(
            new MainStar("gateway-grand-star", "Grand Star Rescue"),
            new SecretStar("gateway-purple-coins", "Gateway's Purple Coins")
        ));
    }

    private static Galaxy createBoosBoneyardGalaxy() {
        return new Galaxy("boos-boneyard", "Boo's Boneyard Galaxy", List.of(
            new MainStar("boos-boneyard-speedster", "Racing the Spooky Speedster")
        ), new TotalStarsCondition(50));
    }

    // ===== TERRACE DOME GALAXIES =====

    private static Galaxy createGoodEggGalaxy() {
        // Main star IDs for comet unlock condition (requires 13+ total stars AND all main stars collected)
        var mainStarIds = List.of("good-egg-dino-piranha", "good-egg-snack", "good-egg-kaliente");
        var cometCondition = new CometUnlockCondition(mainStarIds);
        var purpleCometCondition = new PurpleCometUnlockCondition();
        
        var stars = new java.util.ArrayList<Star>();
        stars.add(new MainStar("good-egg-dino-piranha", "Dino Piranha"));
        stars.add(new MainStar("good-egg-snack", "A Snack of Cosmic Proportions"));
        stars.add(new MainStar("good-egg-kaliente", "King Kaliente's Battle Fleet"));
        stars.add(new SecretStar("good-egg-luigi", "Luigi on the Roof"));
        stars.add(new CometStar("good-egg-dino-speed", "Dino Piranha Speed Run", cometCondition));
        stars.add(new CometStar("good-egg-purple-coins", "Purple Coin Omelet", purpleCometCondition));

        return new Galaxy("good-egg", "Good Egg Galaxy", stars);
    }

    private static Galaxy createHoneyhiveGalaxy() {
        var mainStarIds = List.of("honeyhive-bee-mario", "honeyhive-bugaboom", "honeyhive-trouble-tower");
        var cometCondition = new CometUnlockCondition(mainStarIds);
        var purpleCometCondition = new PurpleCometUnlockCondition();
        
        return new Galaxy("honeyhive", "Honeyhive Galaxy", List.of(
            new MainStar("honeyhive-bee-mario", "Bee Mario Takes Flight"),
            new MainStar("honeyhive-bugaboom", "Big Bad Bugaboom"),
            new MainStar("honeyhive-trouble-tower", "Trouble on the Tower"),
            new CometStar("honeyhive-cosmic-race", "Honeyhive Cosmic Mario Race", cometCondition),
            new CometStar("honeyhive-purple-coins", "The Honeyhive's Purple Coins", purpleCometCondition),
            new SecretStar("honeyhive-luigi", "Luigi in the Honeyhive Kingdom")
        ), new TotalStarsCondition(3));
    }

    private static Galaxy createFlipswitchGalaxy() {
        return new Galaxy("flipswitch", "Flipswitch Galaxy", List.of(
            new MainStar("flipswitch-painting", "Painting the Planet Yellow")
        ), new TotalStarsCondition(7));
    }

    private static Galaxy createLoopdeeloopGalaxy() {
        return new Galaxy("loopdeeloop", "Loopdeeloop Galaxy", List.of(
            new MainStar("loopdeeloop-surfing", "Surfing 101")
        ), new TotalStarsCondition(5));
    }

    private static Galaxy createBowserJrRobotReactorGalaxy() {
        return new Galaxy("bowser-jr-robot", "Bowser Jr.'s Robot Reactor", List.of(
            new MainStar("bowser-jr-robot-megaleg", "Megaleg's Moon")
        ), new TotalStarsCondition(8));
    }

    private static Galaxy createSweetSweetGalaxy() {
        return new Galaxy("sweet-sweet", "Sweet Sweet Galaxy", List.of(
            new MainStar("sweet-sweet-rocky-road", "Rocky Road")
        ), new TotalStarsCondition(7));
    }

    // ===== FOUNTAIN DOME GALAXIES =====

    private static Galaxy createSpaceJunkGalaxy() {
        var mainStarIds = List.of("space-junk-pull-star", "space-junk-kamella", "space-junk-tarantox");
        var cometCondition = new CometUnlockCondition(mainStarIds);
        var purpleCometCondition = new PurpleCometUnlockCondition();
        
        return new Galaxy("space-junk", "Space Junk Galaxy", List.of(
            new MainStar("space-junk-pull-star", "Pull Star Path"),
            new MainStar("space-junk-kamella", "Kamella's Airship Attack"),
            new MainStar("space-junk-tarantox", "Tarantox's Tangled Web"),
            new CometStar("space-junk-pull-star-speed", "Pull Star Path Speed Run", cometCondition),
            new CometStar("space-junk-purple-coins", "Purple Coin Spacewalk", purpleCometCondition),
            new SecretStar("space-junk-yoshi", "Yoshi's Unexpected Appearance")
        ), new TotalStarsCondition(12));
    }

    private static Galaxy createBattlerockGalaxy() {
        var mainStarIds = List.of("battlerock-barrage", "battlerock-breaking-in", "battlerock-topmaniac", "battlerock-garbage-dump");
        var cometCondition = new CometUnlockCondition(mainStarIds);
        var purpleCometCondition = new PurpleCometUnlockCondition();
        
        return new Galaxy("battlerock", "Battlerock Galaxy", List.of(
            new MainStar("battlerock-barrage", "Battlerock Barrage"),
            new MainStar("battlerock-breaking-in", "Breaking into the Battlerock"),
            new MainStar("battlerock-topmaniac", "Topmaniac and the Topman Tribe"),
            new MainStar("battlerock-garbage-dump", "Battlerock's Garbage Dump"),
            new CometStar("battlerock-daredevil", "Topmaniac's Daredevil Run", cometCondition),
            new CometStar("battlerock-purple-coins", "Purple Coins on the Battlerock", purpleCometCondition),
            new SecretStar("battlerock-luigi", "Luigi Under the Saucer"),
            new GreenStar("battlerock-green-star", "Green Power Star")
        ), new TotalStarsCondition(12));
    }

    private static Galaxy createRollingGreenGalaxy() {
        return new Galaxy("rolling-green", "Rolling Green Galaxy", List.of(
            new MainStar("rolling-green-clouds", "Rolling in the Clouds")
        ), new TotalStarsCondition(11));
    }

    private static Galaxy createHurryScurryGalaxy() {
        return new Galaxy("hurry-scurry", "Hurry-Scurry Galaxy", List.of(
            new MainStar("hurry-scurry-satellite", "Shrinking Satellite")
        ), new TotalStarsCondition(18));
    }

    private static Galaxy createBowserStarReactorGalaxy() {
        return new Galaxy("bowser-star", "Bowser's Star Reactor", List.of(
            new MainStar("bowser-star-stronghold", "The Fiery Stronghold")
        ), new TotalStarsCondition(15));
    }

    private static Galaxy createSlingPodGalaxy() {
        return new Galaxy("sling-pod", "Sling Pod Galaxy", List.of(
            new MainStar("sling-pod-sticky-situation", "A Very Sticky Situation")
        ), new TotalStarsCondition(12));
    }

    // ===== KITCHEN DOME GALAXIES =====

    private static Galaxy createBeachBowlGalaxy() {
        var mainStarIds = List.of("beach-bowl-sunken-treasure", "beach-bowl-swim-test", "beach-bowl-secret-cavern", "beach-bowl-wall-jumping");
        var cometCondition = new CometUnlockCondition(mainStarIds);
        var purpleCometCondition = new PurpleCometUnlockCondition();
        
        return new Galaxy("beach-bowl", "Beach Bowl Galaxy", List.of(
            new MainStar("beach-bowl-sunken-treasure", "Sunken Treasure"),
            new MainStar("beach-bowl-swim-test", "Passing the Swim Test"),
            new MainStar("beach-bowl-secret-cavern", "The Secret Undersea Cavern"),
            new MainStar("beach-bowl-wall-jumping", "Wall Jumping up Waterfalls"),
            new CometStar("beach-bowl-cyclone-stone", "Fast Foes on the Cyclone Stone", cometCondition),
            new CometStar("beach-bowl-purple-coins", "Beachcombing for Purple Coins", purpleCometCondition)
        ), new TotalStarsCondition(18));
    }

    private static Galaxy createGhostlyGalaxy() {
        var mainStarIds = List.of("ghostly-bouldergeist", "ghostly-spooky-sprint", "ghostly-matter-splatter");
        var cometCondition = new CometUnlockCondition(mainStarIds);
        var purpleCometCondition = new PurpleCometUnlockCondition();
        
        return new Galaxy("ghostly", "Ghostly Galaxy", List.of(
            new MainStar("ghostly-bouldergeist", "Beware of Bouldergeist"),
            new MainStar("ghostly-spooky-sprint", "A Very Spooky Sprint"),
            new MainStar("ghostly-matter-splatter", "Matter Splatter Mansion"),
            new CometStar("ghostly-daredevil", "Bouldergeist's Daredevil Run", cometCondition),
            new CometStar("ghostly-purple-coins", "Purple Coins in the Bone Pen", purpleCometCondition),
            new SecretStar("ghostly-luigi", "Luigi and the Haunted Mansion")
        ), new TotalStarsCondition(20));
    }

    private static Galaxy createBubbleBreezeGalaxy() {
        return new Galaxy("bubble-breeze", "Bubble Breeze Galaxy", List.of(
            new MainStar("bubble-breeze-poison-swamp", "Through the Poison Swamp")
        ), new TotalStarsCondition(19));
    }

    private static Galaxy createBuoyBaseGalaxy() {
        return new Galaxy("buoy-base", "Buoy Base Galaxy", List.of(
            new MainStar("buoy-base-floating-fortress", "The Floating Fortress"),
            new SecretStar("buoy-base-secret", "The Secret of Buoy Base"),
            new GreenStar("buoy-base-green-star", "Green Power Star")
        ), new TotalStarsCondition(30));
    }

    private static Galaxy createBowserJrAirshipArmadaGalaxy() {
        return new Galaxy("bowser-jr-airship", "Bowser Jr.'s Airship Armada", List.of(
            new MainStar("bowser-jr-airship-sinking", "Sinking the Airships")
        ), new TotalStarsCondition(23));
    }

    private static Galaxy createDripDropGalaxy() {
        return new Galaxy("drip-drop", "Drip Drop Galaxy", List.of(
            new MainStar("drip-drop-giant-eel", "Giant Eel Outbreak")
        ), new TotalStarsCondition(18));
    }

    private static Galaxy createBigmouthGalaxy() {
        return new Galaxy("bigmouth", "Bigmouth Galaxy", List.of(
            new MainStar("bigmouth-gold-bait", "Bigmouth's Gold Bait")
        ), new TotalStarsCondition(33));
    }

    // ===== BEDROOM DOME GALAXIES =====

    private static Galaxy createGustyGardenGalaxy() {
        var mainStarIds = List.of("gusty-garden-bunnies", "gusty-garden-major-burrow", "gusty-garden-gravity-scramble", "gusty-garden-golden-chomp");
        var cometCondition = new CometUnlockCondition(mainStarIds);
        var purpleCometCondition = new PurpleCometUnlockCondition();
        
        return new Galaxy("gusty-garden", "Gusty Garden Galaxy", List.of(
            new MainStar("gusty-garden-bunnies", "Bunnies in the Wind"),
            new MainStar("gusty-garden-major-burrow", "The Dirty Tricks of Major Burrows"),
            new MainStar("gusty-garden-gravity-scramble", "Gusty Garden's Gravity Scramble"),
            new MainStar("gusty-garden-golden-chomp", "The Golden Chomp"),
            new CometStar("gusty-garden-daredevil", "Major Burrow's Daredevil Run", cometCondition),
            new CometStar("gusty-garden-purple-coins", "Purple Coins on the Puzzle Cube", purpleCometCondition)
        ), new TotalStarsCondition(33));
    }

    private static Galaxy createFreezeflameGalaxy() {
        var mainStarIds = List.of("freezeflame-summit", "freezeflame-core", "freezeflame-hot-cold", "freezeflame-baron-bill");
        var cometCondition = new CometUnlockCondition(mainStarIds);
        var purpleCometCondition = new PurpleCometUnlockCondition();
        
        return new Galaxy("freezeflame", "Freezeflame Galaxy", List.of(
            new MainStar("freezeflame-summit", "Conquering the Summit"),
            new MainStar("freezeflame-core", "Freezeflame's Blistering Core"),
            new MainStar("freezeflame-hot-cold", "Hot and Cold Collide"),
            new MainStar("freezeflame-baron-bill", "The Frozen Peak of Barron Bill"),
            new CometStar("freezeflame-cosmic-race", "Frosty Cosmic Mario Race", cometCondition),
            new CometStar("freezeflame-purple-coins", "Purple Coins on the Summit", purpleCometCondition)
        ), new TotalStarsCondition(37));
    }

    private static Galaxy createDustyDuneGalaxy() {
        var mainStarIds = List.of("dusty-dune-desert-winds", "dusty-dune-blasting-sand", "dusty-dune-sand-castle", "dusty-dune-bullet-bill");
        var cometCondition = new CometUnlockCondition(mainStarIds);
        var purpleCometCondition = new PurpleCometUnlockCondition();
        
        return new Galaxy("dusty-dune", "Dusty Dune Galaxy", List.of(
            new MainStar("dusty-dune-desert-winds", "Soaring on the Desert Winds"),
            new MainStar("dusty-dune-blasting-sand", "Blasting through the Sand"),
            new MainStar("dusty-dune-sand-castle", "Sunbaked Sand Castle"),
            new MainStar("dusty-dune-bullet-bill", "Bullet Bill on Your Back"),
            new CometStar("dusty-dune-speed-run", "Sandblast Speed Run", cometCondition),
            new CometStar("dusty-dune-purple-coins", "Purple Coins in the Desert", purpleCometCondition),
            new SecretStar("dusty-dune-pyramid", "Treasure of the Pyramid"),
            new GreenStar("dusty-dune-green-star", "Green Power Star")
        ), new TotalStarsCondition(35));
    }

    private static Galaxy createHoneyclimbGalaxy() {
        return new Galaxy("honeyclimb", "Honeyclimb Galaxy", List.of(
            new MainStar("honeyclimb-sticky-wall", "Scaling the Sticky Wall")
        ), new TotalStarsCondition(35));
    }

    private static Galaxy createBowserDarkMatterPlantGalaxy() {
        return new Galaxy("bowser-dark-matter", "Bowser's Dark Matter Plant", List.of(
            new MainStar("bowser-dark-matter-darkness", "Darkness on the Horizon")
        ), new TotalStarsCondition(33));
    }

    // ===== ENGINE ROOM DOME GALAXIES =====

    private static Galaxy createGoldLeafGalaxy() {
        var mainStarIds = List.of("gold-leaf-star-bunnies", "gold-leaf-cataquack", "gold-leaf-rains-pours", "gold-leaf-big-tree");
        var cometCondition = new CometUnlockCondition(mainStarIds);
        var purpleCometCondition = new PurpleCometUnlockCondition();
        
        return new Galaxy("gold-leaf", "Gold Leaf Galaxy", List.of(
            new MainStar("gold-leaf-star-bunnies", "Star Bunnies on the Hunt"),
            new MainStar("gold-leaf-cataquack", "Cataquack to the Skies"),
            new MainStar("gold-leaf-rains-pours", "When it Rains, it Pours"),
            new MainStar("gold-leaf-big-tree", "The Bell on the Big Tree"),
            new CometStar("gold-leaf-cosmic-race", "Cosmic Mario Forest Race", cometCondition),
            new CometStar("gold-leaf-purple-coins", "Purple Coins in the Woods", purpleCometCondition)
        ), new TotalStarsCondition(45));
    }

    private static Galaxy createSeaSlideGalaxy() {
        var mainStarIds = List.of("sea-slide-guppy", "sea-slide-speeding-penguin", "sea-slide-cosmic-race");
        var cometCondition = new CometUnlockCondition(mainStarIds);
        var purpleCometCondition = new PurpleCometUnlockCondition();
        
        return new Galaxy("sea-slide", "Sea Slide Galaxy", List.of(
            new MainStar("sea-slide-guppy", "Going after Guppy"),
            new MainStar("sea-slide-speeding-penguin", "Faster Than a Speeding Penguin"),
            new MainStar("sea-slide-cosmic-race", "Underwater Cosmic Mario Race"),
            new CometStar("sea-slide-purple-coins", "Purple Coins by the Seaside", purpleCometCondition),
            new CometStar("sea-slide-hungry", "Hurry, He's Hungry", cometCondition),
            new SecretStar("sea-slide-silver-stars", "The Silver Stars of Sea Slide")
        ), new TotalStarsCondition(45));
    }

    private static Galaxy createToyTimeGalaxy() {
        var mainStarIds = List.of("toy-time-mecha-bowser", "toy-time-mecha-mario", "toy-time-cake-lane", "toy-time-fast-foes");
        var cometCondition = new CometUnlockCondition(mainStarIds);
        var purpleCometCondition = new PurpleCometUnlockCondition();
        
        return new Galaxy("toy-time", "Toy Time Galaxy", List.of(
            new MainStar("toy-time-mecha-bowser", "Heavy Metal Mecha-Bowser"),
            new MainStar("toy-time-mecha-mario", "Mario Meets Mario"),
            new MainStar("toy-time-cake-lane", "Bouncing Down Cake Lane"),
            new MainStar("toy-time-fast-foes", "Fast Foes of Toy Time"),
            new CometStar("toy-time-purple-coins", "Luigi's Purple Coins", purpleCometCondition),
            new CometStar("toy-time-flipswitch-chain", "The Flipswitch Chain", cometCondition)
        ), new TotalStarsCondition(47));
    }

    private static Galaxy createBonefinGalaxy() {
        return new Galaxy("bonefin", "Bonefin Galaxy", List.of(
            new MainStar("bonefin-kingfin", "Kingfin's Fearsome Waters")
        ), new TotalStarsCondition(55));
    }

    private static Galaxy createBowserJrLavaReactorGalaxy() {
        return new Galaxy("bowser-jr-lava", "Bowser Jr.'s Lava Reactor", List.of(
            new MainStar("bowser-jr-lava-kaliente", "King Kaliente's Spicy Return")
        ), new TotalStarsCondition(52));
    }

    private static Galaxy createSandSpiralGalaxy() {
        return new Galaxy("sand-spiral", "Sand Spiral Galaxy", List.of(
            new MainStar("sand-spiral-snack", "Choosing a Favorite Snack")
        ), new TotalStarsCondition(45));
    }

    // ===== GARDEN DOME GALAXIES =====

    private static Galaxy createDeepDarkGalaxy() {
        var mainStarIds = List.of("deep-dark-boo-box", "deep-dark-ghost-ship", "deep-dark-guppy-lake", "deep-dark-bubble-blastoff");
        var cometCondition = new CometUnlockCondition(mainStarIds);
        var purpleCometCondition = new PurpleCometUnlockCondition();
        
        return new Galaxy("deep-dark", "Deep Dark Galaxy", List.of(
            new MainStar("deep-dark-boo-box", "Boo in a Box"),
            new MainStar("deep-dark-ghost-ship", "The Underground Ghost Ship"),
            new MainStar("deep-dark-guppy-lake", "Guppy and the Underground Lake"),
            new MainStar("deep-dark-bubble-blastoff", "Bubble Blastoff"),
            new CometStar("deep-dark-daredevil", "Ghost Ship Daredevil Run", cometCondition),
            new CometStar("deep-dark-purple-coins", "Plunder the Purple Coins", purpleCometCondition)
        ), new TotalStarsCondition(60));
    }

    private static Galaxy createDreadnoughtGalaxy() {
        var mainStarIds = List.of("dreadnought-infiltrating", "dreadnought-cannons", "dreadnought-topman-revenge", "dreadnought-garbage-dump");
        var cometCondition = new CometUnlockCondition(mainStarIds);
        var purpleCometCondition = new PurpleCometUnlockCondition();
        
        return new Galaxy("dreadnought", "Dreadnought Galaxy", List.of(
            new MainStar("dreadnought-infiltrating", "Infiltrating the Dreadnought"),
            new MainStar("dreadnought-cannons", "Dreadnought's Colossal Cannons"),
            new MainStar("dreadnought-topman-revenge", "Revenge of the Topman Tribe"),
            new MainStar("dreadnought-garbage-dump", "Dreadnought's Garbage Dump"),
            new CometStar("dreadnought-speed-run", "Topman Tribe Speed Run", cometCondition),
            new CometStar("dreadnought-purple-coins", "Battlestation's Purple Coins", purpleCometCondition)
        ), new TotalStarsCondition(60));
    }

    private static Galaxy createMatterSplatterGalaxy() {
        return new Galaxy("matter-splatter", "Matter Splatter Galaxy", List.of(
            new MainStar("matter-splatter-watch-step", "Watch Your Step")
        ), new TotalStarsCondition(60));
    }

    private static Galaxy createMeltyMoltenGalaxy() {
        var mainStarIds = List.of("melty-molten-sinking-spire", "melty-molten-meteor-storm", "melty-molten-fiery-dino");
        var cometCondition = new CometUnlockCondition(mainStarIds);
        var purpleCometCondition = new PurpleCometUnlockCondition();
        
        return new Galaxy("melty-molten", "Melty Molten Galaxy", List.of(
            new MainStar("melty-molten-sinking-spire", "The Sinking Lava Spire"),
            new MainStar("melty-molten-meteor-storm", "Through the Meteor Storm"),
            new MainStar("melty-molten-fiery-dino", "Fiery Dino Piranha"),
            new CometStar("melty-molten-daredevil", "Lava Spire Daredevil Run", cometCondition),
            new CometStar("melty-molten-purple-coins", "Red-Hot Purple Coins", purpleCometCondition),
            new CometStar("melty-molten-burning-tide", "Burning Tide", cometCondition)
        ), new TotalStarsCondition(60));
    }

    private static Galaxy createSnowCapGalaxy() {
        return new Galaxy("snow-cap", "Snow Cap Galaxy", List.of(
            new MainStar("snow-cap-star-bunnies", "Star Bunnies in the Snow")
        ), new TotalStarsCondition(60));
    }

    private static Galaxy createBowserGalaxyReactorGalaxy() {
        return new Galaxy("bowser-galaxy-reactor", "Bowser's Galaxy Reactor", List.of(
            new MainStar("bowser-galaxy-reactor-fate", "The Fate of the Universe")
        ), new TotalStarsCondition(60));
    }

    // ===== PLANET OF TRIALS GALAXIES =====

    private static Galaxy createRollingGizmoGalaxy() {
        return new Galaxy("rolling-gizmo", "Rolling Gizmo Galaxy", List.of(
            new MainStar("rolling-gizmo-gizmos", "Gizmos, Gears, and Gadgets")
        ), new TotalStarsCondition(58));
    }

    private static Galaxy createBubbleBlastGalaxy() {
        return new Galaxy("bubble-blast", "Bubble Blast Galaxy", List.of(
            new MainStar("bubble-blast-labyrinth", "The Electric Labyrinth")
        ), new TotalStarsCondition(58));
    }

    private static Galaxy createLoopdeeswoopGalaxy() {
        return new Galaxy("loopdeeswoop", "Loopdeeswoop Galaxy", List.of(
            new MainStar("loopdeeswoop-wave", "The Galaxy's Greatest Wave")
        ), new TotalStarsCondition(58));
    }

    // ===== GRAND FINALE GALAXY =====

    private static Galaxy createGrandFinaleGalaxy() {
        return new Galaxy("grand-finale", "Grand Finale Galaxy", List.of(
            new MainStar("grand-finale-star-festival", "The Star Festival")
        ), new GrandFinaleUnlockCondition());
    }

    private GameFactory() {
        // don't instantiate this
    }
}
