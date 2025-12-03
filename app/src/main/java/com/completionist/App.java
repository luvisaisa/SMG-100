package com.completionist;

import com.completionist.model.*;
import com.completionist.progress.*;
import com.completionist.storage.*;
import com.completionist.ui.*;

// main entry point - kicks off the whole thing
public class App {

    public static void main(String[] args) {
        // set up storage and load the game
        StorageService storage = new JsonStorageService();
        Game smg = GameFactory.createSuperMarioGalaxy();

        // pick a profile
        ProfileSelector selector = new ProfileSelector(storage);
        PlayerProfile player = selector.selectProfile();
        selector.close();

        // start the ui and go!
        ConsoleUI ui = new ConsoleUI(smg, player, storage);
        ui.start();

        System.out.println("\nGoodbye! Your progress has been saved.");
    }

    // demo method to show off the progress tracking
    public static void runPhase2Demo() {
        System.out.println("=== The Completionist - Phase 2 Demo ===\n");

        // set up a test game and player
        Game smg = GameFactory.createSuperMarioGalaxy();
        PlayerProfile player = new PlayerProfile("Mario");
        GameProgress progress = player.getOrCreateGameProgress(smg.getId());

        System.out.println("Player: " + player.getDisplayName());
        System.out.println("Spoilers: " + (player.isSpoilersEnabled() ? "ON" : "OFF"));
        System.out.println();

        // collect a few stars to test
        System.out.println("=== Collecting Stars ===");
        progress.getStarProgress("good-egg-dinopiranha").markCollected();
        progress.getStarProgress("good-egg-snack").markCollected();
        progress.getStarProgress("good-egg-kaliente").markCollected();
        progress.touch();  // update timestamp

        System.out.println("✓ Collected: Dino Piranha");
        System.out.println("✓ Collected: A Snack of Cosmic Proportions");
        System.out.println("✓ Collected: King Kaliente's Battle Fleet");
        System.out.println();

        // add some notes and ratings
        StarProgress dinoProg = progress.getStarProgress("good-egg-dinopiranha");
        dinoProg.setNote("Jump on the tail three times, watch for the pattern");
        dinoProg.setDifficultyRating(2);

        StarProgress snackProg = progress.getStarProgress("good-egg-snack");
        snackProg.setNote("Feed the hungry Luma");
        snackProg.setDifficultyRating(1);

        // show galaxy progress
        Galaxy goodEgg = smg.getDomeById("terrace").getGalaxyById("good-egg");
        System.out.println("=== Good Egg Galaxy Progress ===");
        System.out.println(String.format("%s - %d/%d Stars (%.1f%%)",
                goodEgg.getName(),
                goodEgg.getCompletedItems(progress),
                goodEgg.getTotalItems(),
                goodEgg.getCompletionPercentage(progress)));
        System.out.println("━".repeat(50));

        for (Star star : goodEgg.getStars()) {
            boolean collected = progress.isStarCollected(star.getId());
            String checkbox = collected ? "✓" : " ";
            String visibilityLabel = "";

            // handle hidden stars
            if (star.isHiddenByDefault()) {
                boolean revealed = progress.isStarRevealed(star.getId()) || player.isSpoilersEnabled();
                if (!revealed) {
                    visibilityLabel = " ???";
                } else {
                    visibilityLabel = " (Hidden)";
                }
            }

            System.out.println(String.format("  [%s] %s %s%s",
                    checkbox, star.getTypeIcon(),
                    star.isHiddenByDefault() && !progress.isStarRevealed(star.getId()) && !player.isSpoilersEnabled()
                        ? "???" : star.getName(),
                    visibilityLabel));

            // show note if there is one
            StarProgress sp = progress.getStarProgress(star.getId());
            if (sp.getNote() != null && !sp.getNote().isEmpty()) {
                System.out.println("      Note: " + sp.getNote());
            }
            if (sp.getDifficultyRating() != null) {
                System.out.println("      Difficulty: " + sp.getDifficultyStars());
            }
        }
        System.out.println();

        // Overall progress
        System.out.println("=== Overall Progress ===");
        System.out.println(String.format("Game: %s - %d/%d Stars (%.1f%%)",
                smg.getName(),
                smg.getCompletedItems(progress),
                smg.getTotalItems(),
                smg.getCompletionPercentage(progress)));
        System.out.println("Player Profile: " + player);
        System.out.println();

        // Test revealing a hidden star
        System.out.println("=== Revealing Hidden Star ===");
        progress.getStarProgress("good-egg-luigi").setRevealed(true);
        System.out.println("✓ Manually revealed: Luigi on the Roof");
        System.out.println();

        System.out.println("=== Phase 2 Complete! ===");
        System.out.println("✓ StarProgress (collected, notes, difficulty, revealed)");
        System.out.println("✓ GameProgress (lazy creation, aggregates stars)");
        System.out.println("✓ PlayerProfile (multi-game support, spoilers setting)");
        System.out.println("✓ ICompletionTrackable integration with progress");
        System.out.println("✓ Hidden star reveal mechanic");
    }
}
