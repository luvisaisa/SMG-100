package com.completionist.ui;

import com.completionist.model.*;
import com.completionist.progress.*;
import com.completionist.storage.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.utils.NonBlockingReader;
import java.io.IOException;
import static com.completionist.ui.ConsoleUtils.*;
import static com.completionist.ui.ConsoleColors.*;

// handles all the menu navigation and display stuff
public class ConsoleUI {
    private final Game game;
    private final PlayerProfile player;
    private final GameProgress progress;
    private final Terminal terminal;
    private final LineReader lineReader;
    private final StorageService storage;
    private boolean running;
    private final java.util.List<String> unlockNotifications;
    private boolean luigiUnlockPopupShown = false; // Track if we've shown the Luigi unlock popup this session
    private boolean completionPopupShown = false; // Track if we've shown the 100% completion popup this session

    public ConsoleUI(Game game, PlayerProfile player, StorageService storage) {
        this.game = game;
        this.player = player;
        this.progress = player.getOrCreateGameProgress(game.getId());
        this.storage = storage;
        this.running = true;
        this.unlockNotifications = new java.util.ArrayList<>();
        
        // Don't show Luigi unlock popup if player has already used Luigi mode
        // (has any Luigi stars or is currently in Luigi mode)
        this.luigiUnlockPopupShown = player.getCharacterMode() == CharacterMode.LUIGI ||
                                      player.getLuigiStarCount(progress) > 0;
        
        // Don't show 100% completion popup if player already has 248 stars
        // (they've already seen it in a previous session)
        this.completionPopupShown = getTotalStarsCollected() >= 248;

        // Initialize JLine3 terminal with better macOS support
        try {
            this.terminal = TerminalBuilder.builder()
                .system(true)
                .jna(true)  // Enable JNA for better native terminal support on macOS
                .jansi(true)
                .dumb(false)  // Force non-dumb terminal
                .build();

            this.lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
                
            // Log terminal info for debugging (can be removed later)
            System.err.println("Terminal type: " + terminal.getType());
            System.err.println("Terminal supports ANSI: " + (terminal.getType().indexOf("dumb") < 0));
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize terminal", e);
        }

        // Add shutdown hook to save on abrupt termination (e.g., VS Code stop)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                showCursor();  // Make sure cursor is visible on exit
                storage.saveProfile(player);
            } catch (StorageException e) {
                System.err.println("Error saving on shutdown: " + e.getMessage());
            }
        }));

        // Update last played timestamp
        player.updateLastPlayed();
    }

    // -- character mode stuff --

    // star IDs get prefixed with "luigi-" in luigi mode
    private String getEffectiveStarId(String starId) {
        return player.getCharacterMode().getStarPrefix() + starId;
    }

    // how many stars does current character have
    private int getCurrentModeStarCount() {
        if (player.getCharacterMode() == CharacterMode.MARIO) {
            return player.getMarioStarCount(progress);
        } else {
            return player.getLuigiStarCount(progress);
        }
    }

    // total stars both characters - 248 for 100% completion
    // (121 mario + 121 luigi + 3 mario green + 3 luigi green)
    private int getTotalStarsCollected() {
        int marioStars = player.getMarioStarCount(progress);
        int luigiStars = player.getLuigiStarCount(progress);
        GreenStarsUnlockCondition greenCondition = new GreenStarsUnlockCondition();
        int marioGreenStars = greenCondition.getCollectedCount(progress, CharacterMode.MARIO);
        int luigiGreenStars = greenCondition.getCollectedCount(progress, CharacterMode.LUIGI);
        return marioStars + luigiStars + marioGreenStars + luigiGreenStars;
    }

    // 100% = 248 stars
    private boolean isGameComplete() {
        return getTotalStarsCollected() >= 248;
    }

    // count stars in a galaxy for current character
    private int countGalaxyStarsForMode(Galaxy galaxy, CharacterMode mode) {
        String starPrefix = mode.getStarPrefix();
        int count = 0;
        for (Star star : galaxy.getStars()) {
            String effectiveStarId = starPrefix + star.getId();
            if (progress.isStarCollected(effectiveStarId)) {
                count++;
            }
        }
        return count;
    }

    // make a progress bar with character-appropriate colors
    private String createModeProgressBar(int collected, int total, CharacterMode mode) {
        double percentage = total > 0 ? (collected / (double) total) : 0;
        int filled = (int) (percentage * 6);
        int empty = 6 - filled;

        String filledColor = mode == CharacterMode.LUIGI ? GREEN : PROGRESS_FILLED;
        return colored("✦".repeat(Math.max(0, filled)), filledColor)
             + colored("✧".repeat(Math.max(0, empty)), PROGRESS_EMPTY);
    }

    // check if unlock condition is met for current character
    private boolean isModeUnlockConditionMet(UnlockCondition condition) {
        if (condition == null) {
            return true;
        }
        
        // Create a mode-aware progress wrapper for condition checking
        // For TotalStarsCondition, use mode-specific count
        if (condition instanceof TotalStarsCondition) {
            TotalStarsCondition tsc = (TotalStarsCondition) condition;
            return getCurrentModeStarCount() >= tsc.getRequiredStars();
        }
        
        // For GrandFinaleUnlockCondition, check both Mario and Luigi stars
        if (condition instanceof GrandFinaleUnlockCondition) {
            GrandFinaleUnlockCondition gfCondition = (GrandFinaleUnlockCondition) condition;
            return gfCondition.isMetForMode(progress, player.getCharacterMode());
        }
        
        // For GreenStarsUnlockCondition, check mode-specific green stars
        if (condition instanceof GreenStarsUnlockCondition) {
            GreenStarsUnlockCondition gsCondition = (GreenStarsUnlockCondition) condition;
            return gsCondition.isMetForMode(progress, player.getCharacterMode());
        }
        
        // For CometUnlockCondition, check mode-specific stars
        if (condition instanceof CometUnlockCondition) {
            // Check total stars requirement
            if (getCurrentModeStarCount() < 13) {
                return false;
            }
            // The comet condition checks specific star IDs - we need mode-aware checking
            return checkCometConditionForMode((CometUnlockCondition) condition);
        }
        
        // For PurpleCometUnlockCondition, check mode-specific stars
        if (condition instanceof PurpleCometUnlockCondition) {
            String prefix = player.getCharacterMode().getStarPrefix();
            return progress.isStarCollected(prefix + "bowser-galaxy-reactor-fate") &&
                   progress.isStarCollected(prefix + "gateway-purple-coins");
        }
        
        // Default: use original condition check
        return condition.isMet(progress);
    }

    // check comet unlock for current character
    // need 13+ stars AND required main stars collected
    private boolean checkCometConditionForMode(CometUnlockCondition condition) {
        // Check total stars requirement first
        if (getCurrentModeStarCount() < 13) {
            return false;
        }
        
        // Check if required main stars are collected for this mode
        String prefix = player.getCharacterMode().getStarPrefix();
        for (String mainStarId : condition.getMainStarIds()) {
            if (!progress.isStarCollected(prefix + mainStarId)) {
                return false;
            }
        }
        return true;
    }

    // get star icon with right colors for character mode
    // comets are always red, green stars always green
    private String getModeStarIcon(Star star, boolean collected, CharacterMode mode) {
        if (!collected) {
            return colored("☆", STAR_UNCOLLECTED);
        }

        // Comet stars are always red when collected (regardless of mode)
        if (star instanceof CometStar) {
            return colored("☄", COMET_STAR);
        }
        
        // Green stars are always bright green when collected (regardless of mode)
        if (star instanceof GreenStar) {
            return colored("✦", GREEN_STAR);
        }

        // Other stars use mode-based coloring
        String starColor = mode == CharacterMode.LUIGI ? GREEN : STAR_COLLECTED;
        
        // Collected stars have different icons based on type
        if (star instanceof SecretStar) {
            return colored("✦", starColor);  // Secret star
        } else {
            return colored("⭑", starColor);  // Main star
        }
    }

    // main loop - keep showing menu until user quits
    public void start() {
        try {
            while (running) {
                showMainMenu();
            }
        } finally {
            // Save on exit
            save();
            try {
                terminal.close();
            } catch (IOException e) {
                System.err.println("Error closing terminal: " + e.getMessage());
            }
        }
    }

    // save to file
    private void save() {
        try {
            storage.saveProfile(player);
        } catch (StorageException e) {
            System.err.println(colored("Error saving profile: " + e.getMessage(), "\033[31m"));
        }
    }

    // main menu - shows domes and options
    private void showMainMenu() {
        var mode = player.getCharacterMode();
        
        // Filter domes - hide Grand Finale and Planet of Trials until conditions are met
        var allDomes = game.getDomes();
        var domes = new java.util.ArrayList<Dome>();
        for (Dome dome : allDomes) {
            if (dome.getId().equals("grand-finale")) {
                // Only show Grand Finale if unlock condition is met
                if (dome.getUnlockCondition() != null && dome.getUnlockCondition().isMet(progress)) {
                    domes.add(dome);
                }
                // In spoiler mode, show it but locked
                else if (player.isSpoilersEnabled()) {
                    domes.add(dome);
                }
            } else if (dome.getId().equals("planet-of-trials")) {
                // Always show Planet of Trials (but locked until all 3 Green Stars are collected)
                domes.add(dome);
            } else {
                domes.add(dome);
            }
        }
        
        // Build menu options
        var menuOptionsList = new java.util.ArrayList<String>();
        menuOptionsList.add("Browse Galaxies");
        menuOptionsList.add("View Notes");
        menuOptionsList.add("Settings");
        menuOptionsList.add("Save & Exit");
        String[] menuOptions = menuOptionsList.toArray(new String[0]);

        int selectedIndex = 0;
        int totalOptions = domes.size() + menuOptions.length;

        var savedAttributes = terminal.enterRawMode();
        // Completely disable echo in raw mode
        var attrs = terminal.getAttributes();
        attrs.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHO, false);
        attrs.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHONL, false);
        attrs.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ICANON, false);
        terminal.setAttributes(attrs);
        hideCursor();
        try {
            while (true) {
                ScreenBuffer buffer = new ScreenBuffer();
                addTitleToBuffer(buffer, mode);
                addPlayerInfoToBuffer(buffer, player, game, mode);
                addDividerToBuffer(buffer);

                // List all domes with mode-aware progress
                for (int i = 0; i < domes.size(); i++) {
                    String prefix = (i == selectedIndex) ? colored("→ ", CYAN) : "  ";
                    String line = String.format("%s%d. %s", prefix, i + 1, formatDomeForMode(domes.get(i), progress, mode));
                    buffer.addLine(line);
                }

                addDividerToBuffer(buffer);

                // Menu options
                for (int i = 0; i < menuOptions.length; i++) {
                    int optionIndex = domes.size() + i;
                    String prefix = (optionIndex == selectedIndex) ? colored("→ ", CYAN) : "  ";

                    String icon = switch (menuOptions[i]) {
                        case "Browse Galaxies" -> "⋆˚꩜｡";
                        case "View Notes" -> "✉︎";
                        case "Settings" -> "⚙︎";
                        case "Save & Exit" -> "➥";
                        default -> "•";
                    };
                    String line = prefix + colored(icon, CYAN) + " " + menuOptions[i].toLowerCase();
                    buffer.addLine(line);
                }

                addDividerToBuffer(buffer);
                
                // Show hint to unlock Luigi mode if not yet unlocked
                if (!player.isLuigiModeUnlocked()) {
                    int marioStars = player.getMarioStarCount(progress);
                    buffer.addLine(colored("collect " + (120 - marioStars) + " more stars to unlock luigi mode", DIM));
                }
                
                String spaceHint = player.isSpoilersEnabled() ? " · space complete" : "";
                String modeHint = player.isLuigiModeUnlocked() ? " · m/l switch" : "";
                String completionHint = isGameComplete() ? " · w 100%" : "";
                buffer.addLine("↑↓ navigate · enter select" + spaceHint + modeHint + completionHint + " · q quit");
                addStarBufferLines(buffer);

                buffer.printWithArt();
                terminal.flush();

                int key = readKey();

                if (key == 27) { // ESC sequence
                    int next1 = readKey();
                    if (next1 == 91) { // '[' - arrow key
                        int next2 = readKey();
                        if (next2 == 65) { // Up arrow
                            selectedIndex = (selectedIndex - 1 + totalOptions) % totalOptions;
                        } else if (next2 == 66) { // Down arrow
                            selectedIndex = (selectedIndex + 1) % totalOptions;
                        }
                    }
                } else if (key == 10 || key == 13) { // Enter
                    terminal.setAttributes(savedAttributes);
                    try {
                        if (selectedIndex < domes.size()) {
                            showGalaxyList(domes.get(selectedIndex));
                        } else {
                            String selectedOption = menuOptions[selectedIndex - domes.size()];
                            switch (selectedOption) {
                                case "Browse Galaxies" -> {} // Just select dome
                                case "View Notes" -> showNotes();
                                case "Settings" -> showSettings();
                                case "Save & Exit" -> { running = false; return; }
                            }
                            if (selectedOption.equals("Save & Exit")) break;
                        }
                    } finally {
                        savedAttributes = terminal.enterRawMode();
                        // Re-disable echo after returning to raw mode
                        var attrs2 = terminal.getAttributes();
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHO, false);
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHONL, false);
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ICANON, false);
                        terminal.setAttributes(attrs2);
                    }
                } else if (key == 'q' || key == 'Q') {
                    running = false;
                    return;
                } else if (key == ' ' && player.isSpoilersEnabled() && selectedIndex < domes.size()) {
                    // Space - autocomplete dome (spoiler mode only)
                    autocompleteDome(domes.get(selectedIndex));
                } else if ((key == 'l' || key == 'L') && player.isLuigiModeUnlocked()) {
                    // L key - switch to Luigi mode
                    if (player.getCharacterMode() != CharacterMode.LUIGI) {
                        player.setCharacterMode(CharacterMode.LUIGI);
                        mode = CharacterMode.LUIGI;
                        save();
                    }
                } else if ((key == 'm' || key == 'M') && player.isLuigiModeUnlocked()) {
                    // M key - switch to Mario mode
                    if (player.getCharacterMode() != CharacterMode.MARIO) {
                        player.setCharacterMode(CharacterMode.MARIO);
                        mode = CharacterMode.MARIO;
                        save();
                    }
                } else if ((key == 'w' || key == 'W') && isGameComplete()) {
                    // W key - view 100% completion screen (only if game is complete)
                    show100PercentScreen();
                }
            }
        } finally {
            terminal.setAttributes(savedAttributes);
        }
    }

    // read a key in raw mode
    private int readKey() {
        try {
            NonBlockingReader reader = terminal.reader();
            return reader.read(100000); // wait up to 100 seconds
        } catch (IOException e) {
            return -1;
        }
    }

    // show galaxies in a dome
    private void showGalaxyList(Dome dome) {
        var galaxies = dome.getGalaxies();
        var mode = player.getCharacterMode();
        int selectedIndex = 0;

        var savedAttributes = terminal.enterRawMode();
        // Completely disable echo in raw mode
        var attrs = terminal.getAttributes();
        attrs.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHO, false);
        attrs.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHONL, false);
        attrs.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ICANON, false);
        terminal.setAttributes(attrs);
        hideCursor();
        try {
            while (true) {
                ScreenBuffer buffer = new ScreenBuffer();
                addTitleToBuffer(buffer, mode);

                // Dome header
                buffer.addLine(formatDomeForMode(dome, progress, mode));
                buffer.addEmptyLine();
                addDividerToBuffer(buffer);

                // List galaxies with mode-aware progress
                for (int i = 0; i < galaxies.size(); i++) {
                    String prefix = (i == selectedIndex) ? colored("→ ", CYAN) : "  ";
                    Galaxy galaxy = galaxies.get(i);
                    
                    // Count mode-specific star collection
                    int collected = countGalaxyStarsForMode(galaxy, mode);
                    int total = galaxy.getTotalItems();

                    // Check unlock status based on mode's star count
                    int modeStarCount = getCurrentModeStarCount();
                    boolean isUnlocked = galaxy.getUnlockCondition() == null ||
                        (galaxy.getUnlockCondition() instanceof TotalStarsCondition &&
                         modeStarCount >= ((TotalStarsCondition) galaxy.getUnlockCondition()).getRequiredStars());
                    
                    // Build unlock requirement bracket if exists (e.g. [✷ 1])
                    // Padded to 6 chars for alignment: [✷XX] or [   ] for unlocked with no condition
                    String unlockBracket = "      "; // 6 spaces for alignment
                    if (galaxy.getUnlockCondition() != null && galaxy.getUnlockCondition() instanceof TotalStarsCondition) {
                        TotalStarsCondition condition = (TotalStarsCondition) galaxy.getUnlockCondition();
                        int stars = condition.getRequiredStars();
                        // Format: [✷XX] where XX is right-aligned with padding
                        String starsStr = String.format("%2d", stars);
                        // Light blue (BRIGHT_CYAN) for completed requirements, yellow for incomplete
                        String color = isUnlocked ? BRIGHT_CYAN : YELLOW;
                        unlockBracket = colored("[✷" + starsStr + "]", color) + " ";
                    }

                    String line;
                    if (!isUnlocked) {
                        // Locked galaxy - show only [???]
                        line = String.format("%s%s%s %s",
                                prefix,
                                unlockBracket,
                                colored("꩜", DIM),
                                colored("[???]", BRIGHT_YELLOW));
                    } else {
                        // Unlocked galaxy - show full info with mode colors
                        // Pad galaxy name to consistent width (28 chars)
                        String galaxyName = galaxy.getName().toUpperCase();
                        String paddedName = String.format("%-24s", galaxyName);
                        if (paddedName.length() > 24) {
                            paddedName = paddedName.substring(0, 24);
                        }
                        
                        String galaxyColor = mode == CharacterMode.LUIGI ? GREEN : GALAXY;
                        String starColor = mode == CharacterMode.LUIGI ? GREEN : STAR_COLLECTED;
                        
                        line = String.format("%s%s%s [%s] %s  %s",
                                prefix,
                                unlockBracket,
                                colored("꩜", galaxyColor),
                                colored(paddedName, galaxyColor),
                                createModeProgressBar(collected, total, mode),
                                colored("★ " + collected + " / " + total, starColor));
                    }
                    buffer.addLine(line);
                }

                addDividerToBuffer(buffer);
                String spaceHint = player.isSpoilersEnabled() ? " · space complete" : "";
                buffer.addLine("↑↓ navigate · enter select" + spaceHint + " · b back");
                addStarBufferLines(buffer);

                buffer.printWithArt();
                terminal.flush();

                int key = readKey();

                if (key == 27) { // ESC sequence
                    int next1 = readKey();
                    if (next1 == 91) { // '[' - arrow key
                        int next2 = readKey();
                        if (next2 == 65) { // Up arrow
                            selectedIndex = (selectedIndex - 1 + galaxies.size()) % galaxies.size();
                        } else if (next2 == 66) { // Down arrow
                            selectedIndex = (selectedIndex + 1) % galaxies.size();
                        }
                    }
                } else if (key == 10 || key == 13) { // Enter
                    terminal.setAttributes(savedAttributes);
                    try {
                        showStarList(galaxies.get(selectedIndex));
                    } finally {
                        savedAttributes = terminal.enterRawMode();
                        // Re-disable echo after returning to raw mode
                        var attrs2 = terminal.getAttributes();
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHO, false);
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHONL, false);
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ICANON, false);
                        terminal.setAttributes(attrs2);
                    }
                } else if (key == ' ' && player.isSpoilersEnabled()) {
                    // Space - autocomplete galaxy (spoiler mode only)
                    autocompleteGalaxy(galaxies.get(selectedIndex));
                } else if (key == 'b' || key == 'B') {
                    return;
                }
            }
        } finally {
            terminal.setAttributes(savedAttributes);
        }
    }

    // show stars in a galaxy
    private void showStarList(Galaxy galaxy) {
        var stars = galaxy.getStars();
        var mode = player.getCharacterMode();
        int selectedIndex = 0;

        var savedAttributes = terminal.enterRawMode();
        // Completely disable echo in raw mode
        var attrs = terminal.getAttributes();
        attrs.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHO, false);
        attrs.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHONL, false);
        attrs.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ICANON, false);
        terminal.setAttributes(attrs);
        hideCursor();
        try {
            while (true) {
                ScreenBuffer buffer = new ScreenBuffer();
                addTitleToBuffer(buffer, mode);

                // Galaxy header with mode-aware counts
                int collected = countGalaxyStarsForMode(galaxy, mode);
                int total = galaxy.getTotalItems();
                double percentage = total > 0 ? (collected * 100.0 / total) : 0;

                String galaxyColor = mode == CharacterMode.LUIGI ? GREEN : GALAXY;
                String starColor = mode == CharacterMode.LUIGI ? GREEN : STAR_COLLECTED;

                buffer.addLine(String.format("%s [%s]", colored("꩜", galaxyColor), colored(galaxy.getName().toUpperCase(), galaxyColor)));
                buffer.addEmptyLine();
                buffer.addLine(String.format("%s                              %.0f%%", colored("★ " + collected + " / " + total, starColor), percentage));
                buffer.addLine("  " + createModeProgressBar(collected, total, mode));

                addDividerToBuffer(buffer);

                // List stars with mode-aware progress
                for (int i = 0; i < stars.size(); i++) {
                    Star star = stars.get(i);
                    String effectiveStarId = getEffectiveStarId(star.getId());
                    
                    boolean isCollected = progress.isStarCollected(effectiveStarId);
                    boolean isRevealed = progress.isStarRevealed(effectiveStarId);

                    // Check if star has unlock condition and if it's met (using mode-aware star count)
                    boolean hasUnlockCondition = star.getUnlockCondition() != null;
                    boolean conditionMet = hasUnlockCondition && isModeUnlockConditionMet(star.getUnlockCondition());
                    
                    // Auto-reveal if condition is met
                    if (conditionMet && !isRevealed) {
                        progress.getStarProgress(effectiveStarId).setRevealed(true);
                        isRevealed = true;
                    }

                    // Star is visible if:
                    // - It's not hidden by default, OR
                    // - It has been manually revealed, OR
                    // - It has an unlock condition that is now met, OR
                    // - Spoilers are enabled
                    boolean isVisible = !star.isHiddenByDefault() || isRevealed || conditionMet || player.isSpoilersEnabled();

                    String prefix = (i == selectedIndex) ? colored("→ ", CYAN) : "  ";

                    if (!isVisible) {
                        // Hidden star (not unlocked yet or secret not revealed)
                        String hiddenText = (star instanceof CometStar) ? formatLockedStar() : formatHiddenStar();
                        buffer.addLine(String.format("%s%d. %s", prefix, i + 1, hiddenText));
                    } else {
                        // Visible star with mode-appropriate coloring
                        String icon = getModeStarIcon(star, isCollected, mode);
                        // Green Stars get bright green color when collected, regardless of mode
                        String nameColor;
                        if (isCollected && star instanceof GreenStar) {
                            nameColor = GREEN_STAR;
                        } else if (isCollected) {
                            nameColor = mode == CharacterMode.LUIGI ? GREEN : STAR_COLLECTED;
                        } else {
                            nameColor = STAR_UNCOLLECTED;
                        }
                        buffer.addLine(String.format("%s%d. %s [%s]", prefix, i + 1, icon,
                            colored(star.getName().toUpperCase(), nameColor)));

                        // Show note if exists
                        StarProgress sp = progress.getStarProgress(effectiveStarId);
                        if (sp.getNote() != null && !sp.getNote().isEmpty()) {
                            buffer.addLine(colored("   note: " + sp.getNote(), DIM));
                        }
                        if (sp.getDifficultyRating() != null) {
                            buffer.addLine(colored("   difficulty: " + sp.getDifficultyStars(), DIM));
                        }
                    }
                }

                addDividerToBuffer(buffer);

                // Display unlock notifications if any
                if (!unlockNotifications.isEmpty()) {
                    for (String notification : unlockNotifications) {
                        buffer.addLine(notification);
                    }
                    buffer.addEmptyLine();
                }

                buffer.addLine("↑↓ navigate · space toggle · r reveal");
                buffer.addLine("n note · d difficulty · b back");
                addStarBufferLines(buffer);

                buffer.printWithArt();
                terminal.flush();

                int key = readKey();

                if (key == 27) { // ESC sequence
                    int next1 = readKey();
                    if (next1 == 91) { // '[' - arrow key
                        int next2 = readKey();
                        if (next2 == 65) { // Up arrow
                            selectedIndex = (selectedIndex - 1 + stars.size()) % stars.size();
                        } else if (next2 == 66) { // Down arrow
                            selectedIndex = (selectedIndex + 1) % stars.size();
                        }
                    }
                } else if (key == ' ' || key == 10 || key == 13) { // Space or Enter - toggle star
                    Star selectedStar = stars.get(selectedIndex);
                    String effectiveId = getEffectiveStarId(selectedStar.getId());
                    // Check if visible before toggling
                    boolean canToggle = !selectedStar.isHiddenByDefault() ||
                                      progress.isStarRevealed(effectiveId) ||
                                      player.isSpoilersEnabled();
                    if (canToggle) {
                        toggleStar(selectedStar);
                    }
                } else if (key == 'r' || key == 'R') {
                    // Reveal hidden star
                    Star selectedStar = stars.get(selectedIndex);
                    String effectiveId = getEffectiveStarId(selectedStar.getId());
                    if (selectedStar.isHiddenByDefault() && !progress.isStarRevealed(effectiveId)) {
                        progress.getStarProgress(effectiveId).setRevealed(true);
                        save();
                    }
                } else if (key == 'n' || key == 'N') {
                    terminal.setAttributes(savedAttributes);
                    try {
                        addNoteToStar(stars.get(selectedIndex));
                    } finally {
                        savedAttributes = terminal.enterRawMode();
                        // Re-disable echo after returning to raw mode
                        var attrs2 = terminal.getAttributes();
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHO, false);
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHONL, false);
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ICANON, false);
                        terminal.setAttributes(attrs2);
                    }
                } else if (key == 'd' || key == 'D') {
                    terminal.setAttributes(savedAttributes);
                    try {
                        setDifficulty(stars.get(selectedIndex));
                    } finally {
                        savedAttributes = terminal.enterRawMode();
                        // Re-disable echo after returning to raw mode
                        var attrs2 = terminal.getAttributes();
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHO, false);
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHONL, false);
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ICANON, false);
                        terminal.setAttributes(attrs2);
                    }
                } else if (key == 'b' || key == 'B') {
                    return;
                }
            }
        } finally {
            terminal.setAttributes(savedAttributes);
        }
    }

    private void toggleStar(Star star) {
        String effectiveId = getEffectiveStarId(star.getId());
        StarProgress sp = progress.getStarProgress(effectiveId);
        if (sp.isCollected()) {
            sp.markUncollected();
        } else {
            sp.markCollected();
            // Check for unlocks only when collecting (not uncollecting)
            checkForUnlocks();
        }
        progress.touch();
        save(); // Auto-save after change
    }

    // toggle all stars in galaxy (spoiler mode only)
    // if all collected -> uncollect, otherwise collect all
    private void autocompleteGalaxy(Galaxy galaxy) {
        if (!player.isSpoilersEnabled()) return;
        
        // Check if all stars are already collected
        boolean allCollected = true;
        for (Star star : galaxy.getStars()) {
            String effectiveId = getEffectiveStarId(star.getId());
            if (!progress.getStarProgress(effectiveId).isCollected()) {
                allCollected = false;
                break;
            }
        }
        
        // Toggle: if all collected, uncollect all; otherwise collect all
        for (Star star : galaxy.getStars()) {
            String effectiveId = getEffectiveStarId(star.getId());
            StarProgress sp = progress.getStarProgress(effectiveId);
            if (allCollected) {
                sp.markUncollected();
            } else {
                if (!sp.isCollected()) {
                    sp.markCollected();
                }
                if (star.isHiddenByDefault() && !sp.isRevealed()) {
                    sp.setRevealed(true);
                }
            }
        }
        progress.touch();
        checkForUnlocks();
        save();
    }

    // toggle all stars in dome (spoiler mode only)
    private void autocompleteDome(Dome dome) {
        if (!player.isSpoilersEnabled()) return;
        
        // Check if all stars in dome are already collected
        boolean allCollected = true;
        outer:
        for (Galaxy galaxy : dome.getGalaxies()) {
            for (Star star : galaxy.getStars()) {
                String effectiveId = getEffectiveStarId(star.getId());
                if (!progress.getStarProgress(effectiveId).isCollected()) {
                    allCollected = false;
                    break outer;
                }
            }
        }
        
        // Toggle: if all collected, uncollect all; otherwise collect all
        for (Galaxy galaxy : dome.getGalaxies()) {
            for (Star star : galaxy.getStars()) {
                String effectiveId = getEffectiveStarId(star.getId());
                StarProgress sp = progress.getStarProgress(effectiveId);
                if (allCollected) {
                    sp.markUncollected();
                } else {
                    if (!sp.isCollected()) {
                        sp.markCollected();
                    }
                    if (star.isHiddenByDefault() && !sp.isRevealed()) {
                        sp.setRevealed(true);
                    }
                }
            }
        }
        progress.touch();
        checkForUnlocks();
        save();
    }

    private void addNoteToStar(Star star) {
        try {
            clearScreen();
            showCursor();
            String effectiveId = getEffectiveStarId(star.getId());
            String note = lineReader.readLine("Enter note (or empty to clear): ").trim();
            if (note.isEmpty()) {
                progress.getStarProgress(effectiveId).setNote(null);
            } else {
                progress.getStarProgress(effectiveId).setNote(note);
            }
            progress.touch();
            save();
        } catch (Exception e) {
            // Ignore input errors
        }
    }

    private void setDifficulty(Star star) {
        try {
            clearScreen();
            showCursor();
            String effectiveId = getEffectiveStarId(star.getId());
            String input = lineReader.readLine("Enter difficulty (1-5, or 0 to clear): ").trim();
            if (!input.isEmpty()) {
                int difficulty = Integer.parseInt(input);
                if (difficulty == 0) {
                    progress.getStarProgress(effectiveId).setDifficultyRating(null);
                } else if (difficulty >= 1 && difficulty <= 5) {
                    progress.getStarProgress(effectiveId).setDifficultyRating(difficulty);
                }
                progress.touch();
                save();
            }
        } catch (NumberFormatException ignored) {
            // Invalid number, just return
        }
    }

    private void showNotes() {
        var savedAttributes = terminal.enterRawMode();
        // Completely disable echo in raw mode
        var attrs = terminal.getAttributes();
        attrs.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHO, false);
        attrs.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHONL, false);
        attrs.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ICANON, false);
        terminal.setAttributes(attrs);
        try {
            clearScreen();
            hideCursor();

            CharacterMode mode = player.getCharacterMode();
            String modePrefix = mode.getStarPrefix();
            String themeColor = mode == CharacterMode.LUIGI ? LUIGI_THEME : CYAN;
            String starColor = mode == CharacterMode.LUIGI ? LUIGI_STAR : STAR_COLLECTED;

            ScreenBuffer buffer = new ScreenBuffer();
            addTitleToBuffer(buffer);
            buffer.addLine(colored("✉︎ YOUR NOTES (" + mode.getDisplayName().toUpperCase() + " MODE)", themeColor));
            addDividerToBuffer(buffer);

            var notes = progress.getAllNotes();
            boolean hasNotes = false;
            
            for (var entry : notes.entrySet()) {
                String storedId = entry.getKey();
                // Filter notes by current mode
                boolean matchesMode;
                String baseStarId;
                if (mode == CharacterMode.LUIGI) {
                    // In Luigi mode, only show notes with luigi- prefix
                    matchesMode = storedId.startsWith(modePrefix);
                    baseStarId = matchesMode ? storedId.substring(modePrefix.length()) : storedId;
                } else {
                    // In Mario mode, only show notes without prefix
                    matchesMode = !storedId.startsWith("luigi-");
                    baseStarId = storedId;
                }
                
                if (matchesMode) {
                    Star star = game.findStarById(baseStarId);
                    if (star != null) {
                        hasNotes = true;
                        String starIcon = getModeStarIcon(star, true, mode);
                        buffer.addLine(String.format("%s [%s]", starIcon,
                            colored(star.getName().toUpperCase(), starColor)));
                        buffer.addLine(colored("  " + entry.getValue(), NOTE));
                        buffer.addEmptyLine();
                    }
                }
            }
            
            if (!hasNotes) {
                buffer.addLine(colored("no notes yet for " + mode.getDisplayName().toLowerCase() + " mode", DIM));
            }

            addDividerToBuffer(buffer);
            buffer.addLine("press any key to continue");
            addStarBufferLines(buffer);

            buffer.printWithArt();
            terminal.flush();
            readKey();
        } finally {
            terminal.setAttributes(savedAttributes);
        }
    }

    private void showSettings() {
        var savedAttributes = terminal.enterRawMode();
        // Completely disable echo in raw mode
        var attrs = terminal.getAttributes();
        attrs.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHO, false);
        attrs.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHONL, false);
        attrs.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ICANON, false);
        terminal.setAttributes(attrs);
        hideCursor();
        try {
            while (true) {
                ScreenBuffer buffer = new ScreenBuffer();
                addTitleToBuffer(buffer);
                buffer.addLine(colored("⚙︎ SETTINGS & PROFILE", CYAN));
                addDividerToBuffer(buffer);

                buffer.addLine(String.format("player name: %s", player.getDisplayName()));
                buffer.addLine(String.format("star bits: %s %d", colored("₊⊹", CYAN), player.getStarBits()));
                buffer.addLine(String.format("spoilers: %s", player.isSpoilersEnabled() ? colored("ON", CYAN) : colored("OFF", DIM)));
                buffer.addLine(String.format("play time: %s", formatPlayTime(player.getPlayTimeMinutes())));

                addDividerToBuffer(buffer);
                buffer.addLine("n change name · + add star bits · - remove star bits");
                buffer.addLine("t log play time · s toggle spoilers · b back");
                addStarBufferLines(buffer);

                buffer.printWithArt();
                terminal.flush();

                int key = readKey();

                if (key == 's' || key == 'S') {
                    player.setSpoilersEnabled(!player.isSpoilersEnabled());
                    save();
                } else if (key == 'n' || key == 'N') {
                    terminal.setAttributes(savedAttributes);
                    try {
                        clearScreen();
                        showCursor();
                        String name = lineReader.readLine("New name: ").trim();
                        if (!name.isEmpty()) {
                            player.setDisplayName(name);
                            save();
                        }
                    } catch (Exception ignored) {
                    } finally {
                        savedAttributes = terminal.enterRawMode();
                        // Re-disable echo after returning to raw mode
                        var attrs2 = terminal.getAttributes();
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHO, false);
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHONL, false);
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ICANON, false);
                        terminal.setAttributes(attrs2);
                    }
                } else if (key == '+' || key == '=') {
                    terminal.setAttributes(savedAttributes);
                    try {
                        clearScreen();
                        showCursor();
                        String input = lineReader.readLine("Add star bits (amount): ").trim();
                        if (!input.isEmpty()) {
                            int amount = Integer.parseInt(input);
                            if (amount > 0) {
                                player.addStarBits(amount);
                                save();
                            }
                        }
                    } catch (NumberFormatException ignored) {
                    } finally {
                        savedAttributes = terminal.enterRawMode();
                        // Re-disable echo after returning to raw mode
                        var attrs2 = terminal.getAttributes();
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHO, false);
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHONL, false);
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ICANON, false);
                        terminal.setAttributes(attrs2);
                    }
                } else if (key == '-' || key == '_') {
                    terminal.setAttributes(savedAttributes);
                    try {
                        clearScreen();
                        showCursor();
                        String input = lineReader.readLine("Remove star bits (amount): ").trim();
                        if (!input.isEmpty()) {
                            int amount = Integer.parseInt(input);
                            if (amount > 0) {
                                int newAmount = Math.max(0, player.getStarBits() - amount);
                                player.setStarBits(newAmount);
                                save();
                            }
                        }
                    } catch (NumberFormatException ignored) {
                    } finally {
                        savedAttributes = terminal.enterRawMode();
                        // Re-disable echo after returning to raw mode
                        var attrs2 = terminal.getAttributes();
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHO, false);
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHONL, false);
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ICANON, false);
                        terminal.setAttributes(attrs2);
                    }
                } else if (key == 't' || key == 'T') {
                    terminal.setAttributes(savedAttributes);
                    try {
                        clearScreen();
                        showCursor();
                        System.out.println(colored("Log Play Time", CYAN));
                        System.out.println("Current play time: " + formatPlayTime(player.getPlayTimeMinutes()));
                        System.out.println();
                        System.out.println("Enter time to add (examples: 30m, 1h, 1h 30m, 90):");
                        String input = lineReader.readLine("> ").trim().toLowerCase();
                        if (!input.isEmpty()) {
                            long minutesToAdd = parsePlayTimeInput(input);
                            if (minutesToAdd > 0) {
                                player.addPlayTime(minutesToAdd);
                                save();
                                System.out.println(colored("✓ Added " + formatPlayTime(minutesToAdd) + " play time", CYAN));
                                System.out.println("New total: " + formatPlayTime(player.getPlayTimeMinutes()));
                                Thread.sleep(1500);
                            } else if (minutesToAdd == 0) {
                                System.out.println(colored("No time added.", DIM));
                                Thread.sleep(1000);
                            }
                        }
                    } catch (Exception ignored) {
                    } finally {
                        savedAttributes = terminal.enterRawMode();
                        var attrs2 = terminal.getAttributes();
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHO, false);
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHONL, false);
                        attrs2.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ICANON, false);
                        terminal.setAttributes(attrs2);
                    }
                } else if (key == 'b' || key == 'B') {
                    return;
                }
            }
        } finally {
            terminal.setAttributes(savedAttributes);
        }
    }

    private String formatPlayTime(long minutes) {
        long hours = minutes / 60;
        long mins = minutes % 60;
        return String.format("%dh %dm", hours, mins);
    }

    // parse play time input like "30m", "1h", "1h 30m", or just "90" for minutes
    private long parsePlayTimeInput(String input) {
        long totalMinutes = 0;
        
        // Remove extra spaces and normalize
        input = input.trim().toLowerCase().replaceAll("\\s+", " ");
        
        // Try to parse as plain number (minutes)
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException ignored) {
            // Not a plain number, try to parse formatted time
        }
        
        // Match hours pattern (e.g., "1h", "2h")
        java.util.regex.Pattern hoursPattern = java.util.regex.Pattern.compile("(\\d+)\\s*h");
        java.util.regex.Matcher hoursMatcher = hoursPattern.matcher(input);
        if (hoursMatcher.find()) {
            totalMinutes += Long.parseLong(hoursMatcher.group(1)) * 60;
        }
        
        // Match minutes pattern (e.g., "30m", "45m")
        java.util.regex.Pattern minutesPattern = java.util.regex.Pattern.compile("(\\d+)\\s*m");
        java.util.regex.Matcher minutesMatcher = minutesPattern.matcher(input);
        if (minutesMatcher.find()) {
            totalMinutes += Long.parseLong(minutesMatcher.group(1));
        }
        
        return totalMinutes;
    }

    // check if we just unlocked anything new
    // shows notifications and auto-reveals stuff
    private void checkForUnlocks() {
        unlockNotifications.clear();
        CharacterMode mode = player.getCharacterMode();
        int currentModeStarCount = getCurrentModeStarCount();
        String themeColor = mode == CharacterMode.LUIGI ? BRIGHT_GREEN : BRIGHT_YELLOW;

        // Check if Luigi mode was JUST unlocked (Mario reached 120 stars for the first time)
        // Show popup if in Mario mode, count >= 120, and we haven't shown it yet this session
        if (mode == CharacterMode.MARIO && currentModeStarCount >= 120 && !luigiUnlockPopupShown) {
            luigiUnlockPopupShown = true;
            showLuigiUnlockPopup();
            // Automatically switch to Luigi mode after the popup
            player.setCharacterMode(CharacterMode.LUIGI);
            save();
        }

        // Check if 100% completion was JUST achieved (248 total stars)
        if (isGameComplete() && !completionPopupShown) {
            completionPopupShown = true;
            show100PercentScreen();
        }

        // Check all galaxies for unlock conditions
        for (Dome dome : game.getDomes()) {
            for (Galaxy galaxy : dome.getGalaxies()) {
                // Check if galaxy just unlocked (based on current mode's star count)
                if (galaxy.getUnlockCondition() != null && galaxy.getUnlockCondition() instanceof TotalStarsCondition) {
                    TotalStarsCondition condition = (TotalStarsCondition) galaxy.getUnlockCondition();
                    // Notify if we just reached the exact threshold
                    if (currentModeStarCount == condition.getRequiredStars()) {
                        unlockNotifications.add(colored("⭐ " + galaxy.getName() + " has been unlocked!", themeColor));
                    }
                }

                // Check all stars in this galaxy for unlock conditions
                for (Star star : galaxy.getStars()) {
                    if (star.getUnlockCondition() != null) {
                        String effectiveStarId = getEffectiveStarId(star.getId());
                        boolean wasRevealed = progress.isStarRevealed(effectiveStarId);
                        boolean isNowUnlocked = isModeUnlockConditionMet(star.getUnlockCondition());

                        if (!wasRevealed && isNowUnlocked) {
                            // Auto-reveal the star for current mode
                            progress.getStarProgress(effectiveStarId).setRevealed(true);

                            // Add notification based on star type
                            if (star instanceof CometStar) {
                                unlockNotifications.add(colored("☄ " + star.getName() + " comet is in orbit", themeColor));
                            } else if (star instanceof SecretStar) {
                                unlockNotifications.add(colored("✦ A secret star has been revealed in " + galaxy.getName(), themeColor));
                            } else {
                                unlockNotifications.add(colored("⭑ " + star.getName() + " has been revealed in " + galaxy.getName(), themeColor));
                            }
                        }
                    }
                }
            }
        }
    }

    // big popup when you unlock luigi mode!
    private void showLuigiUnlockPopup() {
        clearScreen();
        hideCursor();
        
        ScreenBuffer buffer = new ScreenBuffer();
        buffer.addEmptyLine();
        buffer.addEmptyLine();
        buffer.addLine(colored("╔══════════════════════════════════════════════════════════╗", GREEN));
        buffer.addLine(colored("║                                                          ║", GREEN));
        buffer.addLine(colored("║     " + BOLD + "★ ★ ★  CONGRATULATIONS!  ★ ★ ★" + RESET + GREEN + "                 ║", GREEN));
        buffer.addLine(colored("║                                                          ║", GREEN));
        buffer.addLine(colored("║            🟢  LUIGI MODE UNLOCKED!  🟢                  ║", GREEN));
        buffer.addLine(colored("║                                                          ║", GREEN));
        buffer.addLine(colored("║     You've collected all 120 Power Stars as Mario!       ║", GREEN));
        buffer.addLine(colored("║                                                          ║", GREEN));
        buffer.addLine(colored("║     Now play through the game again as Luigi to          ║", GREEN));
        buffer.addLine(colored("║     unlock the Grand Finale Galaxy!                      ║", GREEN));
        buffer.addLine(colored("║                                                          ║", GREEN));
        buffer.addLine(colored("║     Press [L] anytime to switch to Luigi Mode            ║", GREEN));
        buffer.addLine(colored("║     Press [M] to switch back to Mario Mode               ║", GREEN));
        buffer.addLine(colored("║                                                          ║", GREEN));
        buffer.addLine(colored("╚══════════════════════════════════════════════════════════╝", GREEN));
        buffer.addEmptyLine();
        buffer.addEmptyLine();
        buffer.addLine(colored("            Press any key to continue...", DIM));
        
        buffer.printWithArt();
        terminal.flush();
        readKey();
    }

    // 100% completion celebration screen - rainbow colors!
    private void show100PercentScreen() {
        clearScreen();
        hideCursor();
        
        // rainbow colors for celebration effect
        String[] rainbowColors = {
            "\033[91m", // Bright Red
            "\033[93m", // Bright Yellow
            "\033[92m", // Bright Green
            "\033[96m", // Bright Cyan
            "\033[94m", // Bright Blue
            "\033[95m"  // Bright Magenta
        };
        
        ScreenBuffer buffer = new ScreenBuffer();
        buffer.addEmptyLine();
        buffer.addEmptyLine();
        
        // Create rainbow-colored border
        String topBorder =    "╔══════════════════════════════════════╗";
        String bottomBorder = "╚══════════════════════════════════════╝";
        String emptyContent = "                                        ";
        
        StringBuilder rainbowTop = new StringBuilder();
        for (int i = 0; i < topBorder.length(); i++) {
            rainbowTop.append(rainbowColors[i % rainbowColors.length]).append(topBorder.charAt(i));
        }
        rainbowTop.append(RESET);
        
        StringBuilder rainbowBottom = new StringBuilder();
        for (int i = 0; i < bottomBorder.length(); i++) {
            rainbowBottom.append(rainbowColors[i % rainbowColors.length]).append(bottomBorder.charAt(i));
        }
        rainbowBottom.append(RESET);
        
        buffer.addLine(rainbowTop.toString());
        buffer.addLine(colored("║", rainbowColors[0]) + emptyContent + colored("║", rainbowColors[5]));
        
        // Rainbow stars header - using ASCII * instead of ★
        String starsLine = "* * *  100% COMPLETE!  * * *";
        StringBuilder rainbowStars = new StringBuilder();
        for (int i = 0; i < starsLine.length(); i++) {
            rainbowStars.append(rainbowColors[i % rainbowColors.length]).append(starsLine.charAt(i));
        }
        rainbowStars.append(RESET);
        buffer.addLine(colored("║", rainbowColors[0]) + "      " + BOLD + rainbowStars + "      " + colored("║", rainbowColors[5]));
        
        buffer.addLine(colored("║", rainbowColors[1]) + emptyContent + colored("║", rainbowColors[4]));
        buffer.addLine(colored("║", rainbowColors[1]) + colored("  + 248/248 POWER STARS COLLECTED +   ", BRIGHT_YELLOW) + colored("║", rainbowColors[4]));
        buffer.addLine(colored("║", rainbowColors[2]) + emptyContent + colored("║", rainbowColors[3]));
        buffer.addLine(colored("║", rainbowColors[2]) + colored("      a true SMG completionist!       ", BRIGHT_CYAN) + colored("║", rainbowColors[3]));
        buffer.addLine(colored("║", rainbowColors[3]) + emptyContent + colored("║", rainbowColors[2]));
        buffer.addLine(colored("║", rainbowColors[3]) + colored("  * Mario: 121 stars + 3 green stars  ", PURPLE) + colored("║", rainbowColors[2]));
        buffer.addLine(colored("║", rainbowColors[4]) + colored("  * Luigi: 121 stars + 3 green stars  ", GREEN) + colored("║", rainbowColors[1]));
        buffer.addLine(colored("║", rainbowColors[4]) + emptyContent + colored("║", rainbowColors[1]));
        buffer.addLine(colored("║", rainbowColors[5]) + colored("        Thank you for playing!        ", DIM) + colored("║", rainbowColors[0]));
        buffer.addLine(colored("║", rainbowColors[5]) + emptyContent + colored("║", rainbowColors[0]));
        buffer.addLine(rainbowBottom.toString());
        buffer.addEmptyLine();
        buffer.addEmptyLine();
        buffer.addLine(colored("    Press any key to continue...", DIM));
        
        buffer.printWithArt();
        terminal.flush();
        readKey();
    }
}
