package com.completionist;

import com.completionist.model.*;
import com.completionist.progress.*;
import com.completionist.storage.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;

class AppTest {
    private Game smg;
    private PlayerProfile player;
    private GameProgress progress;

    @BeforeEach
    void setUp() {
        smg = GameFactory.createSuperMarioGalaxy();
        player = new PlayerProfile("TestPlayer");
        progress = player.getOrCreateGameProgress(smg.getId());
    }

    // Phase 1 Tests
    @Test void gameFactoryCreatesSuperMarioGalaxy() {
        assertNotNull(smg, "Game should not be null");
        assertEquals("super-mario-galaxy", smg.getId());
        assertEquals("Super Mario Galaxy", smg.getName());
        assertEquals(9, smg.getDomes().size(), "Should have 9 domes (Gateway, Terrace, Fountain, Kitchen, Bedroom, Engine Room, Garden, Planet of Trials, Grand Finale)");
    }

    @Test void goodEggGalaxyHasSixStars() {
        Dome terrace = smg.getDomeById("terrace");
        assertNotNull(terrace, "Terrace dome should exist");

        Galaxy goodEgg = terrace.getGalaxyById("good-egg");
        assertNotNull(goodEgg, "Good Egg Galaxy should exist");
        assertEquals(6, goodEgg.getStars().size(), "Good Egg should have 6 stars");
    }

    @Test void starTypesHaveCorrectIcons() {
        assertEquals("⭐", new MainStar("test", "Test").getTypeIcon());
        assertEquals("🌟", new SecretStar("test", "Test").getTypeIcon());
        assertEquals("☄️", new CometStar("test", "Test").getTypeIcon());
        assertEquals("🌠", new GrandStar("test", "Test").getTypeIcon());
    }

    @Test void findStarByIdWorks() {
        Star star = smg.findStarById("good-egg-dino-piranha");

        assertNotNull(star, "Star should be found");
        assertEquals("Dino Piranha", star.getName());
        assertInstanceOf(MainStar.class, star);
    }

    // Phase 2 Tests
    @Test void newProfileStartsWithZeroStars() {
        assertEquals(0, player.getTotalStarsCollected());
        assertEquals(0, progress.getCollectedCount());
    }

    @Test void markingStarCollectedUpdatesCount() {
        progress.getStarProgress("good-egg-dino-piranha").markCollected();

        assertTrue(progress.isStarCollected("good-egg-dino-piranha"));
        assertEquals(1, progress.getCollectedCount());
        assertEquals(1, player.getTotalStarsCollected());
    }

    @Test void galaxyCompletionPercentageCalculatesCorrectly() {
        Galaxy goodEgg = smg.getDomeById("terrace").getGalaxyById("good-egg");

        // Collect 2 out of 6 stars
        progress.getStarProgress("good-egg-dino-piranha").markCollected();
        progress.getStarProgress("good-egg-snack").markCollected();

        assertEquals(2, goodEgg.getCompletedItems(progress));
        assertEquals(6, goodEgg.getTotalItems());
        assertEquals(33.33, goodEgg.getCompletionPercentage(progress), 0.01);
    }

    @Test void notesAndDifficultyPersist() {
        StarProgress sp = progress.getStarProgress("good-egg-dino-piranha");
        sp.setNote("Test note");
        sp.setDifficultyRating(3);

        assertEquals("Test note", sp.getNote());
        assertEquals(3, sp.getDifficultyRating());
        assertEquals("★★★☆☆", sp.getDifficultyStars());
    }

    @Test void uncollectingStarReducesCount() {
        StarProgress sp = progress.getStarProgress("good-egg-dino-piranha");
        sp.markCollected();
        assertEquals(1, progress.getCollectedCount());

        sp.markUncollected();
        assertEquals(0, progress.getCollectedCount());
        assertFalse(progress.isStarCollected("good-egg-dino-piranha"));
    }

    @Test void difficultyRatingValidation() {
        StarProgress sp = progress.getStarProgress("good-egg-dino-piranha");

        assertThrows(IllegalArgumentException.class, () -> sp.setDifficultyRating(0));
        assertThrows(IllegalArgumentException.class, () -> sp.setDifficultyRating(6));
        assertDoesNotThrow(() -> sp.setDifficultyRating(1));
        assertDoesNotThrow(() -> sp.setDifficultyRating(5));
        assertDoesNotThrow(() -> sp.setDifficultyRating(null));
    }

    @Test void hiddenStarsCanBeRevealed() {
        assertFalse(progress.isStarRevealed("good-egg-luigi"));

        progress.getStarProgress("good-egg-luigi").setRevealed(true);

        assertTrue(progress.isStarRevealed("good-egg-luigi"));
    }

    @Test void spoilersSettingWorks() {
        assertFalse(player.isSpoilersEnabled());

        player.setSpoilersEnabled(true);

        assertTrue(player.isSpoilersEnabled());
    }

    @Test void completionPercentageWithNullProgress() {
        Galaxy goodEgg = smg.getDomeById("terrace").getGalaxyById("good-egg");

        assertEquals(0, goodEgg.getCompletedItems(null));
        assertEquals(0.0, goodEgg.getCompletionPercentage(null));
    }

    // Storage Tests
    @Test void profileSaveAndLoadRoundTrip(@TempDir Path tempDir) throws StorageException {
        // Create storage service with temp directory
        StorageService storage = new JsonStorageService(tempDir);
        
        // Create a profile with some progress
        PlayerProfile profile = new PlayerProfile("test-player", "Test Player");
        GameProgress gameProgress = profile.getOrCreateGameProgress("super-mario-galaxy");
        gameProgress.getStarProgress("good-egg-dino-piranha").markCollected();
        gameProgress.getStarProgress("good-egg-snack").markCollected();
        gameProgress.getStarProgress("good-egg-snack").setNote("Easy star!");
        gameProgress.getStarProgress("good-egg-snack").setDifficultyRating(2);
        profile.setSpoilersEnabled(true);
        
        // Save the profile
        storage.saveProfile(profile);
        assertTrue(storage.profileExists("test-player"));
        
        // Load the profile back
        PlayerProfile loaded = storage.loadProfile("test-player");
        
        // Verify all data persisted correctly
        assertEquals("test-player", loaded.getPlayerId());
        assertEquals("Test Player", loaded.getDisplayName());
        assertTrue(loaded.isSpoilersEnabled());
        
        GameProgress loadedProgress = loaded.getOrCreateGameProgress("super-mario-galaxy");
        assertTrue(loadedProgress.isStarCollected("good-egg-dino-piranha"));
        assertTrue(loadedProgress.isStarCollected("good-egg-snack"));
        assertEquals("Easy star!", loadedProgress.getStarProgress("good-egg-snack").getNote());
        assertEquals(2, loadedProgress.getStarProgress("good-egg-snack").getDifficultyRating());
        assertEquals(2, loadedProgress.getCollectedCount());
    }

    @Test void listProfilesWorks(@TempDir Path tempDir) throws StorageException {
        StorageService storage = new JsonStorageService(tempDir);
        
        // Initially empty
        assertTrue(storage.listProfiles().isEmpty());
        
        // Create profiles
        storage.saveProfile(new PlayerProfile("player1", "Player One"));
        storage.saveProfile(new PlayerProfile("player2", "Player Two"));
        
        // List should have both
        var profiles = storage.listProfiles();
        assertEquals(2, profiles.size());
        assertTrue(profiles.contains("player1"));
        assertTrue(profiles.contains("player2"));
    }

    @Test void deleteProfileWorks(@TempDir Path tempDir) throws StorageException {
        StorageService storage = new JsonStorageService(tempDir);
        
        storage.saveProfile(new PlayerProfile("to-delete", "Delete Me"));
        assertTrue(storage.profileExists("to-delete"));
        
        storage.deleteProfile("to-delete");
        assertFalse(storage.profileExists("to-delete"));
    }
}
