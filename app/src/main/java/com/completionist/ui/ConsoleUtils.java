package com.completionist.ui;

import com.completionist.model.*;
import com.completionist.progress.*;
import static com.completionist.ui.ConsoleColors.*;

// helper methods for console ui formatting
public class ConsoleUtils {
    private static final int PROGRESS_BAR_LENGTH = 10;

    // clear the screen
    public static void clearScreen() {
        // move cursor home and clear below
        System.out.print("\033[H\033[J");
        System.out.flush();
    }

    // move cursor to top without clearing (less flicker)
    public static void cursorHome() {
        System.out.print("\033[H");
        System.out.flush();
    }

    // hide cursor
    public static void hideCursor() {
        System.out.print("\033[?25l");
        System.out.flush();
    }

    // show cursor
    public static void showCursor() {
        System.out.print("\033[?25h");
        System.out.flush();
    }

    // print the title
    public static void printTitle() {
        System.out.println(colored("★ THE COMPLETIONIST ★", TITLE));
        System.out.println(colored("galaxy tracker", DIM));
        System.out.println();
    }

    // add title to screen buffer
    public static void addTitleToBuffer(ScreenBuffer buffer) {
        buffer.addLine(colored("★ THE COMPLETIONIST ★", TITLE));
        buffer.addLine(colored("galaxy tracker", DIM));
        buffer.addEmptyLine();
    }

    // title with mode indicator (mario/luigi)
    public static void addTitleToBuffer(ScreenBuffer buffer, CharacterMode mode) {
        String titleColor = mode == CharacterMode.LUIGI ? GREEN : TITLE;
        String modeIndicator = mode == CharacterMode.LUIGI ? 
            colored(" [LUIGI MODE]", BRIGHT_GREEN) : "";
        
        buffer.addLine(colored("★ THE COMPLETIONIST ★", titleColor) + modeIndicator);
        buffer.addLine(colored("galaxy tracker", DIM));
        buffer.addEmptyLine();
    }

    // print a divider line
    public static void printDivider() {
        String divider = "─────────────────────── ⋆⋅☆⋅⋆ ───────────────────────";
        System.out.println(colored(divider, DIVIDER));
    }

    // add divider to buffer
    public static void addDividerToBuffer(ScreenBuffer buffer) {
        String divider = "─────────────────────── ⋆⋅☆⋅⋆ ───────────────────────";
        buffer.addLine(colored(divider, DIVIDER));
    }

    // print player info header
    public static void printPlayerInfo(PlayerProfile player, Game game) {
        GameProgress progress = player.getGameProgress(game.getId());
        int collectedStars = progress != null ? game.getCompletedItems(progress) : 0;
        int totalStars = game.getTotalItems();

        // player name, playtime and date
        String playerName = colored("[" + player.getDisplayName().toUpperCase() + "]", GALAXY);
        String timeInfo = player.getFormattedPlayTime() + " · " + formatDate(player.getLastPlayed());
        System.out.println("player: " + playerName + "        " + colored(timeInfo, DIM));
        
        // starbits + star count
        System.out.println(formatStarBits(player.getStarBits()) + "        " + formatStarCount(collectedStars, totalStars));

        System.out.println();
    }

    // add player info to buffer
    public static void addPlayerInfoToBuffer(ScreenBuffer buffer, PlayerProfile player, Game game) {
        GameProgress progress = player.getGameProgress(game.getId());
        int collectedStars = progress != null ? game.getCompletedItems(progress) : 0;
        int totalStars = game.getTotalItems();

        String playerName = colored("[" + player.getDisplayName().toUpperCase() + "]", GALAXY);
        String timeInfo = player.getFormattedPlayTime() + " · " + formatDate(player.getLastPlayed());
        buffer.addLine("player: " + playerName + "        " + colored(timeInfo, DIM));
        
        buffer.addLine(formatStarBits(player.getStarBits()) + "        " + formatStarCount(collectedStars, totalStars));

        buffer.addEmptyLine();
    }

    // player info with mode-specific star counts
    // layout:
    // player: [NAME]        Xh Ym · YYYY-MM-DD
    // ₊⊹ starbits        Mario/Luigi: ★ X / Y + ✦ X/3
    // total: ★ X / 242 + ✦ X/6  (only if luigi unlocked)
    public static void addPlayerInfoToBuffer(ScreenBuffer buffer, PlayerProfile player, Game game, CharacterMode mode) {
        GameProgress progress = player.getGameProgress(game.getId());
        
        // mode-specific star counts (excludes green stars)
        int marioStars = progress != null ? player.getMarioStarCount(progress) : 0;
        int luigiStars = progress != null ? player.getLuigiStarCount(progress) : 0;
        int currentModeStars = mode == CharacterMode.LUIGI ? luigiStars : marioStars;
        int totalStarsPerMode = 121;
        
        // Total across both modes: 242 = 121 Mario + 121 Luigi
        int totalAllStars = marioStars + luigiStars;
        int grandTotal = 242;
        
        // Count green stars for current mode and total
        GreenStarsUnlockCondition greenCondition = new GreenStarsUnlockCondition();
        int marioGreenStars = progress != null ? greenCondition.getCollectedCount(progress, CharacterMode.MARIO) : 0;
        int luigiGreenStars = progress != null ? greenCondition.getCollectedCount(progress, CharacterMode.LUIGI) : 0;
        int currentModeGreenStars = mode == CharacterMode.LUIGI ? luigiGreenStars : marioGreenStars;
        int totalGreenStars = marioGreenStars + luigiGreenStars;
        
        String starColor = mode == CharacterMode.LUIGI ? GREEN : STAR_COLLECTED;
        
        // Only show mode label and total if Luigi mode is unlocked
        boolean luigiUnlocked = player.isLuigiModeUnlocked();
        
        // Line 1: player name on left, playtime on right
        String playerName = colored("[" + player.getDisplayName().toUpperCase() + "]", GALAXY);
        buffer.addLine("player: " + playerName + "        " + colored(player.getFormattedPlayTime(), DIM));
        
        // Line 2: star count: x/121 + green x/3
        String starCount = colored("★ " + currentModeStars + "/" + totalStarsPerMode, starColor);
        String greenCount = colored("+ ✦ " + currentModeGreenStars + "/3", GREEN_STAR);
        
        if (luigiUnlocked) {
            String modeLabel = mode == CharacterMode.LUIGI ? "Luigi" : "Mario";
            buffer.addLine(modeLabel + ": " + starCount + " " + greenCount);
            // Line 3: total star count + starbits on right
            String totalStarCount = colored("★", YELLOW) + colored(" " + totalAllStars + "/" + grandTotal, BRIGHT_CYAN);
            String totalGreenCount = colored("+ ✦ " + totalGreenStars + "/6", GREEN_STAR);
            buffer.addLine("total: " + totalStarCount + " " + totalGreenCount + "        " + formatStarBits(player.getStarBits()));
        } else {
            // Line 2: star count + starbits on right (no third line when Luigi not unlocked)
            buffer.addLine(starCount + " " + greenCount + "        " + formatStarBits(player.getStarBits()));
        }

        buffer.addEmptyLine();
    }

    // format star count like "★ 42 / 120"
    public static String formatStarCount(int collected, int total) {
        return colored("★ " + collected + " / " + total, STAR_COLLECTED);
    }

    // format starbits with icon
    public static String formatStarBits(int amount) {
        return colored("₊⊹ " + amount, BLUE);
    }

    // simple date format yyyy-mm-dd
    public static String formatDate(java.time.LocalDateTime date) {
        return date.toLocalDate().toString();
    }

    // progress bar with filled/empty stars
    public static void printProgressBar(int collected, int total) {
        int filled = total > 0 ? (int) ((collected / (double) total) * PROGRESS_BAR_LENGTH) : 0;
        int empty = PROGRESS_BAR_LENGTH - filled;

        String bar = colored("✦".repeat(Math.max(0, filled)), PROGRESS_FILLED)
                   + colored("✧".repeat(Math.max(0, empty)), PROGRESS_EMPTY);

        System.out.println("  " + bar);
    }

    // pick the right star icon based on type and if collected
    public static String getStarIcon(Star star, boolean collected) {
        if (!collected) {
            return colored("☆", STAR_UNCOLLECTED);
        }

        // Collected stars have different icons
        if (star instanceof MainStar) {
            return colored("⭑", STAR_COLLECTED);
        } else if (star instanceof SecretStar) {
            return colored("⭑!", STAR_COLLECTED);
        } else if (star instanceof CometStar) {
            return colored("★彡", STAR_COLLECTED);
        } else if (star instanceof GrandStar) {
            return colored("𖤓", STAR_COLLECTED);
        }

        return colored("☆", STAR_UNCOLLECTED);
    }

    // shows a locked star
    public static String formatLockedStar() {
        return colored("⏾ [LOCKED]", LOCKED);
    }

    // shows a hidden star (for spoiler-free mode)
    public static String formatHiddenStar() {
        return colored("??? [HIDDEN]", HIDDEN);
    }

    // print dome header with star count
    public static void printDome(Dome dome, GameProgress progress) {
        int collected = dome.getCompletedItems(progress);
        int total = dome.getTotalItems();

        String line = String.format("%s [%s]%s%s",
                colored("⌂", DOME),
                colored(dome.getName().toUpperCase(), DOME),
                " ".repeat(Math.max(1, 40 - dome.getName().length())),
                formatStarCount(collected, total));

        System.out.println(line);
    }

    // format dome with lock status and star counts
    public static String formatDome(Dome dome, GameProgress progress) {
        int collected = dome.getCompletedItems(progress);
        int total = dome.getTotalItems();
        
        // Check unlock status
        boolean isUnlocked = dome.isUnlocked(progress);
        
        // Build unlock requirement bracket (aligned to 6 chars like galaxies)
        String unlockBracket = "      "; // 6 spaces for alignment
        if (dome.getUnlockCondition() != null && dome.getUnlockCondition() instanceof TotalStarsCondition) {
            TotalStarsCondition condition = (TotalStarsCondition) dome.getUnlockCondition();
            int stars = condition.getRequiredStars();
            String starsStr = String.format("%2d", stars);
            // Light blue (BRIGHT_CYAN) for completed, yellow for incomplete
            String color = isUnlocked ? BRIGHT_CYAN : YELLOW;
            unlockBracket = colored("[✷" + starsStr + "]", color) + " ";
        }
        
        if (!isUnlocked) {
            // Locked dome - show only [???]
            return String.format("%s%s %s",
                    unlockBracket,
                    colored("⌂", DIM),
                    colored("[???]", BRIGHT_YELLOW));
        } else {
            // Unlocked dome - show full info
            String domeName = dome.getName().toUpperCase();
            String paddedName = String.format("%-20s", domeName);
            if (paddedName.length() > 20) {
                paddedName = paddedName.substring(0, 20);
            }
            
            return String.format("%s%s [%s] %s",
                    unlockBracket,
                    colored("⌂", DOME),
                    colored(paddedName, DOME),
                    formatStarCount(collected, total));
        }
    }

    // format dome for a specific character (mario/luigi)
    // tracks progress separately for each character
    public static String formatDomeForMode(Dome dome, GameProgress progress, CharacterMode mode) {
        // Calculate mode-specific star counts
        String starPrefix = mode.getStarPrefix();
        int collected = 0;
        int total = dome.getTotalItems();
        
        // Count stars collected for this mode
        for (Galaxy galaxy : dome.getGalaxies()) {
            for (Star star : galaxy.getStars()) {
                String effectiveStarId = starPrefix + star.getId();
                if (progress.isStarCollected(effectiveStarId)) {
                    collected++;
                }
            }
        }
        
        // Check unlock status based on current mode's star count
        int modeStarCount = countModeStars(progress, mode);
        boolean isUnlocked = dome.getUnlockCondition() == null;
        
        if (!isUnlocked && dome.getUnlockCondition() instanceof TotalStarsCondition) {
            isUnlocked = modeStarCount >= ((TotalStarsCondition) dome.getUnlockCondition()).getRequiredStars();
        } else if (!isUnlocked && dome.getUnlockCondition() instanceof GreenStarsUnlockCondition) {
            GreenStarsUnlockCondition gsCondition = (GreenStarsUnlockCondition) dome.getUnlockCondition();
            isUnlocked = gsCondition.isMetForMode(progress, mode);
        } else if (!isUnlocked && dome.getUnlockCondition() instanceof GrandFinaleUnlockCondition) {
            GrandFinaleUnlockCondition gfCondition = (GrandFinaleUnlockCondition) dome.getUnlockCondition();
            isUnlocked = gfCondition.isMetForMode(progress, mode);
        }
        
        // Build unlock requirement bracket (aligned to 6 chars like galaxies)
        String unlockBracket = "      "; // 6 spaces for alignment
        if (dome.getUnlockCondition() != null && dome.getUnlockCondition() instanceof TotalStarsCondition) {
            TotalStarsCondition condition = (TotalStarsCondition) dome.getUnlockCondition();
            int stars = condition.getRequiredStars();
            String starsStr = String.format("%2d", stars);
            // Light blue (BRIGHT_CYAN) for completed, yellow for incomplete
            String color = isUnlocked ? BRIGHT_CYAN : YELLOW;
            unlockBracket = colored("[✷" + starsStr + "]", color) + " ";
        }
        
        if (!isUnlocked) {
            // Locked dome - show only [???]
            return String.format("%s%s %s",
                    unlockBracket,
                    colored("⌂", DIM),
                    colored("[???]", BRIGHT_YELLOW));
        } else {
            // Unlocked dome - show full info with mode-appropriate colors
            String domeName = dome.getName().toUpperCase();
            String paddedName = String.format("%-20s", domeName);
            if (paddedName.length() > 20) {
                paddedName = paddedName.substring(0, 20);
            }
            
            String domeColor = mode == CharacterMode.LUIGI ? GREEN : DOME;
            String starColor = mode == CharacterMode.LUIGI ? GREEN : STAR_COLLECTED;
            
            return String.format("%s%s [%s] %s",
                    unlockBracket,
                    colored("⌂", domeColor),
                    colored(paddedName, domeColor),
                    colored("★ " + collected + " / " + total, starColor));
        }
    }

    // count stars collected for mario or luigi mode
    private static int countModeStars(GameProgress progress, CharacterMode mode) {
        String prefix = mode.getStarPrefix();
        return (int) progress.getAllStarProgress().values().stream()
                .filter(sp -> sp.getStarId().startsWith(prefix) && sp.isCollected())
                .count();
    }

    // print galaxy header with completion %
    public static void printGalaxy(Galaxy galaxy, GameProgress progress) {
        double percentage = galaxy.getCompletionPercentage(progress);

        String line = String.format("%s [%s]%s%.0f%%",
                colored("꩜", GALAXY),
                colored(galaxy.getName().toUpperCase(), GALAXY),
                " ".repeat(Math.max(1, 40 - galaxy.getName().length())),
                percentage);

        System.out.println(line);
    }

    // shows the prompt
    public static void printPrompt() {
        System.out.print(colored("> ", PROMPT));
    }

    // center text in given width
    public static String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }

    // cosmic background ascii art - looks cool
    private static final String[] ASCII_ART_PATTERN = {
        "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀",
        "⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀",
        "⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐⠀⠀⠀⠐",
        "⠄⠠⠀⠀⠄⠠⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀⠄⠐⢀⠔⡂⡊⠊⠢⠐⡀⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀⠄⠐⠀⠀",
        "⠀⠀⠠⠀⠀⠀⠠⠀⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⢂⠠⠊⡢⠘⡄⡈⠠⡛⠕⢎⠀⠄⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠠⠀",
        "⠂⠠⠀⠀⠂⠠⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠄⠅⡨⢐⠡⢊⠐⡄⡀⠂⠀⠀⠀⠀⠀⠂⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠠⠀⠀",
        "⠀⠀⠠⠀⠀⠀⠠⠀⠀⠀⠐⠀⠀⠀⡀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⢂⢐⠁⢊⠐⢄⠡⢊⠐⢅⠢⡨⡀⠑⢀⠐⠀⠀⠀⠂⠐⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠠⠀",
        "⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠁⠠⠀⠀⠀⠐⠀⠁⠀⠂⠀⠁⠀⠂⠀⠁⠀⠂⠀⠁⠀⠂⠀⠁⠀⠂⠀⠁⠀⠂⠀⠁⠀⠂⠀⠁⠀⠂⢀⠑⡠⠁⡠⠁⠢⠐⡁⢊⠌⠢⢂⠢⡈⢊⠄⡈⠄⠂⠐⠀⠀⡀⠂⠈⠀⠐⠀⠁⠀⠂⠀⠁⠀⠂⠀⠁⠀⠂⠀⠁⠀⠂⠀⠁⠀⠂⠀⠁⠀⠂⠀⠁⠀⠂⠀⠁⠀⠂⠀⠁⠀⠂⠀⠁⠀⠐⠀⠀⠀",
        "⠀⠀⠄⠠⠀⠀⠄⠠⠀⠀⠄⢀⠀⠀⠂⠠⠀⠀⠄⠠⠀⠈⠀⠠⠀⠈⠀⠠⠀⠈⠀⠠⠀⠈⠀⠠⠀⠈⠀⠠⠀⠈⠀⠠⠀⠈⠀⠠⠀⡂⠁⠠⠀⠄⠊⠐⠡⡈⠢⡁⢕⢁⢊⠌⡂⠢⡐⠄⠑⢀⠄⢀⠀⠀⠠⠀⠀⠀⠄⠠⠀⠈⠀⠠⠀⠈⠀⠠⠀⠈⠀⠠⠀⠈⠀⠠⠀⠈⠀⠠⠀⠈⠀⠠⠀⠈⠀⠠⠀⠈⠀⠠⠀⠈⠀⠠⠀⠁⠀⠂",
        "⠂⢀⠀⠀⠠⠀⠀⠀⠠⠀⠀⠀⠀⠠⠀⠀⠠⠀⠀⠀⠀⠄⠀⠀⠠⠀⠀⠀⠠⠀⠀⠀⠠⠀⠀⠀⠠⠀⠀⠀⠠⠀⠀⠀⠠⠀⠀⡐⡈⠀⠌⠀⠂⢀⠁⠑⠠⡈⠢⡈⠢⡡⢂⠪⡈⠢⢂⠌⠂⠄⠐⢀⠀⠠⠀⠀⠂⠠⡀⠠⡀⢂⠂⠂⠀⠄⠀⠀⠠⠀⠀⠀⠠⠀⠀⠀⠠⠀⠀⠀⠠⠀⠀⠀⠠⠀⠀⠀⠠⠀⠀⠀⠠⠀⠀⠀⠠⠀⠀⠀",
        "⠀⠀⠀⠄⠀⠀⠂⠠⠀⠀⠂⠀⠁⠀⠀⠄⠀⠀⠂⠐⠀⠀⠀⠂⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠔⠀⠐⠀⠀⠠⠀⠀⢁⠐⠠⢁⠌⠢⢂⠕⡈⡢⢑⠐⢄⠁⡐⠀⠀⠢⠀⠄⠢⢈⠢⠈⡂⠐⠀⠀⢀⠂⠀⠀⠂⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐⠀⠀⠂⠐",
        "⠄⠠⠀⠀⠄⠠⠀⠀⠠⠀⠀⠁⠀⠐⠀⠀⠀⠂⠀⠀⠄⢀⠁⠀⠂⠠⠀⠀⠄⠠⠀⠀⠄⠠⠀⠀⠄⠠⠀⠀⠄⠠⠀⠀⠄⠨⢈⠂⠈⠀⠀⠀⠀⠀⠀⡀⠐⠀⡂⡈⢂⠅⢌⠢⢂⠅⢂⠢⠀⠄⠀⠄⠀⠑⡈⠐⡀⠂⠡⢀⠁⡐⢀⢔⠁⠀⠈⠀⡀⠐⠀⠀⠄⠠⠀⠀⠄⠠⠀⠀⠄⠠⠀⠀⠄⠠⠀⠀⠄⠠⠀⠀⠄⠠⠀⠀⠄⠠⠀⠀",
        "⠀⠀⠀⠄⠀⠀⠠⠀⠀⠀⠐⠀⠈⠀⠠⠀⠁⠈⠀⡀⠀⠀⢀⠀⠀⠀⠠⠀⠀⠀⠠⠀⠀⢠⠊⠈⠌⢈⠌⢊⠐⠡⢊⠐⠌⠠⠁⠄⠐⠀⠀⠈⠀⠀⠀⠀⠀⠁⠠⠐⠄⢊⠄⠣⢂⣵⠾⣶⣦⠀⠀⠀⠀⠀⠈⠠⠀⠁⠂⠐⢄⠢⡱⡑⠀⠁⠀⡀⠀⠀⠄⢀⠀⠀⠀⠄⠀⠀⠀⠠⠀⠀⠀⠠⠀⠀⠀⠠⠀⠀⠀⠠⠀⠀⠀⠠⠀⠀⠀⠄",
        "⡀⠐⠀⠀⠄⠠⠀⠀⠂⠀⠁⠀⠄⢀⠀⠀⠄⢀⠀⠀⠠⠀⠀⠀⠐⠀⠀⠀⠂⠠⠀⠀⠄⠰⡈⢂⠀⠀⠀⠀⠁⠁⠄⢅⠈⡂⢁⠀⠀⢀⡶⢿⣶⡄⠀⠀⠁⠈⡀⢊⠠⠡⡨⡑⢰⣿⡀⣸⣯⡷⠀⠀⠀⠀⠀⠀⠡⠀⡡⢑⢌⣪⠣⠀⠀⢀⠀⠀⠠⠀⠀⠀⠀⠄⠀⠀⠄⠀⠁⠀⢀⠀⠈⠀⢀⠀⠈⠀⢀⠀⠈⠀⢀⠀⠈⠀⠀⠠⠀⠀",
        "⠀⠀⠄⢀⠀⠀⠠⠀⠈⠀⠠⠀⠀⠀⠀⠄⠀⠀⠀⠠⠀⠀⠂⢀⠈⠀⠐⠀⠀⠀⠄⢀⠀⠀⠑⡌⢐⢁⠀⡀⢂⢈⡐⢀⠂⠔⠀⠀⠀⣿⡃⢀⣿⣿⠀⠀⠐⠀⠄⠂⢌⠂⢆⢌⠸⣷⢿⣿⣟⣿⡃⠀⠀⠀⠀⠄⢀⠁⢎⠊⡴⠣⠀⢀⠀⠀⠀⠠⠀⠀⠂⠠⠀⠀⠐⠀⠀⠈⠀⡀⠀⠀⠄⢀⠀⠀⠄⢀⠀⠀⠄⢀⠀⠀⠄⢀⠈⠀⢀⠀",
        "⠄⠠⠀⠀⠀⠠⠀⠀⠄⢀⠀⠀⠂⠠⠀⠀⠄⢀⠈⠀⠀⠠⠀⠀⢀⠀⠀⠁⠀⠄⠀⠀⢀⠀⠀⠘⢄⠑⠔⡠⠂⢔⠠⠁⢌⠐⠠⠀⢘⣿⣝⢾⣻⣿⡇⠀⠀⠄⡈⢂⠢⡑⢌⢢⠡⣿⢿⣯⣿⣟⣿⠀⡀⠂⠠⠀⠄⠐⠈⡎⠊⠀⠀⠀⢀⠀⠁⠀⠀⠄⠀⠀⠠⠀⠈⠀⠠⠀⠀⠀⠠⠀⠀⠀⢀⠀⠀⠀⢀⠀⠀⠀⢀⠀⠀⠀⠀⡀⠀⠀",
        "⠀⠀⠀⠄⠈⠀⠀⠄⠀⠀⠀⠄⠀⠀⠄⢀⠀⠀⠀⡀⠈⠀⠀⠠⠀⠀⠄⠐⠀⠀⠄⠠⠀⠀⠄⢀⠈⠢⡈⠢⢑⠅⡐⢁⢂⢁⠂⡀⠨⣿⣷⣻⣟⣿⡇⠀⠀⠄⡨⢐⠅⡪⢢⢃⢖⠸⣿⣿⣽⡿⡿⢀⠔⡁⠔⠡⡨⡀⡑⠄⠈⠀⠠⠀⠀⠀⡀⠐⠀⠀⠀⠂⠀⠀⠄⠠⠀⠀⠂⠠⠀⠀⠂⢀⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠂⠀⠀⠀⠄",
        "⠄⠠⠀⠀⠄⢀⠀⠀⠄⠠⠀⠀⠄⢀⠀⠀⢀⠀⠀⠀⡀⢀⠈⠀⠀⡀⠀⠀⠄⢀⠀⠀⢀⠀⠀⠀⠀⠀⠑⠤⣀⠃⠌⡠⢁⠢⠐⠄⠀⣿⣿⢾⡿⣿⠀⠀⡐⢐⠄⢕⢌⢪⢢⢣⢕⣕⡙⠿⡻⢟⡣⣣⢝⢌⢎⡒⡕⡬⢐⠡⠀⢀⠀⠀⠄⢀⠀⠀⠄⠀⠁⠈⠀⡀⠀⠀⠀⠄⠀⠀⠄⢀⠀⠀⢀⠀⠁⠀⠄⠀⠁⠀⠄⠀⠁⠈⠀⠠⠀⠀",
        "⠀⠀⠠⠀⠀⠀⠀⠠⠀⠀⠀⠄⠀⠀⢀⠀⠀⠀⠄⢀⠀⠀⠀⡀⠀⠀⠀⠄⠀⠀⠀⠠⠀⠀⠄⠀⠁⠀⡀⠀⠈⠔⡡⠠⡁⢎⢈⢊⢄⠈⠿⣿⠿⠋⢀⠐⠌⡄⢕⠱⡨⡣⡣⡳⡱⣎⢞⡝⡮⣳⡹⣪⢎⢷⡩⣞⢜⢮⠂⡕⠀⠀⠀⠠⠀⠀⢀⠀⠀⠈⠀⢀⠀⠀⠠⠀⠁⠀⠠⠀⠀⠀⠀⠄⠀⠀⠠⠀⠀⠈⠀⡀⠀⠈⠀⡀⠀⠀⠄⢀",
        "⠀⠠⠀⠐⠀⡀⠁⠀⠀⠄⠐⠀⠠⠀⠀⠀⠄⠠⠀⠀⢀⠀⠀⠀⠠⠀⠐⠀⠀⠄⠈⠀⠀⡀⠀⠈⠀⢀⠀⠈⠀⠂⠆⡐⢌⠢⡑⢔⢄⠣⠢⡠⢂⠢⢂⢑⠌⡢⣑⢍⢎⣎⡳⡹⣎⢞⢮⡺⡵⣣⢟⡼⣳⡣⣯⡪⡳⡭⡣⡊⡀⠀⠁⠀⡀⢀⠀⠀⠄⠠⠀⠀⢀⠀⠀⠀⠄⢀⠀⠐⠀⠄⠐⠀⠀⠄⠀⠐⠀⠠⠀⠀⠀⠄⠀⠀⠀⠄⠀⠀",
        "⠁⠀⡀⢀⠀⠀⠀⠄⠐⠀⠀⡀⠀⠐⠀⠠⠀⠀⡀⢀⠀⠀⠄⠠⠀⠂⠀⠠⠀⠀⠄⢀⠀⠀⠀⠄⠀⠀⢀⠀⠀⠡⡑⠠⡑⢌⠪⡂⢎⠜⡔⡡⢢⢑⢅⢪⡘⡔⡬⡪⡲⣕⢝⢮⢮⣫⡳⣝⢾⣕⢟⡮⣳⡝⣮⢝⣝⢮⡪⢢⠀⠈⠀⠀⠀⠀⠀⡀⠀⠀⠠⠀⠀⠀⠄⠠⠀⠀⢀⠀⠀⠀⡀⠠⠀⠀⠄⠀⠐⠀⠀⠄⠐⠀⠀⠄⠐⠀⢀⠀",
        "⠄⠀⠀⠀⠀⠠⠀⠀⡀⢀⠀⠀⠀⠄⠀⠀⠄⠀⠀⠀⠀⡀⠀⠀⡀⠀⠀⠂⠀⡀⠀⠀⠀⠄⠐⠀⠠⠀⠀⠀⠠⠀⢪⠀⠜⡐⢕⡑⢕⠪⡜⣘⢆⡣⣪⢢⠳⣜⢎⡞⣕⢧⣫⢗⢷⣕⢟⡮⣳⡭⡻⣮⢳⡽⣎⢿⡜⣧⢣⡑⠀⡀⠈⠀⠠⠀⠀⠀⠠⠀⠀⠂⠀⠄⠀⠀⡀⢀⠀⠀⠄⢀⠀⠀⡀⠠⠀⠈⠀⢀⠀⠀⡀⢀⠀⠀⡀⢀⠀⠀",
        "⠀⠈⠀⢀⠈⠀⢀⠀⠀⠀⠀⠀⠂⠐⠀⢀⠀⠈⠀⡀⠀⠀⡀⢀⠀⠈⠀⢀⠀⠀⡀⠀⠂⠀⢀⠀⠀⠂⢀⠀⠂⠀⠡⡂⠈⠨⡢⡱⢍⢞⢜⢎⢮⡪⣪⢳⡹⣎⢾⡸⡧⣳⢵⡫⡷⣭⢻⢮⣳⣝⢯⣺⡳⡽⣮⡳⣝⢮⡢⡑⠀⠀⢀⠀⠐⠀⠄⠀⠂⠐⠀⠀⠐⠀⢀⠀⠀⠀⢀⠀⠀⠀⢀⠀⠀⠀⢀⠀⠐⠀⠀⡀⠀⠀⢀⠀⠀⠀⠀⡀",
        "⡀⠀⠂⠀⠀⡀⠀⠀⠄⠀⠁⠈⠀⠀⠀⠀⡀⢀⠀⠀⠠⠀⠀⠀⠀⡀⠀⠀⡀⠀⠀⠈⠀⢀⠀⠀⠠⠀⠀⠀⡀⢀⠀⢊⠀⠐⠈⡪⡪⣣⡫⣎⢧⡳⣹⡪⣳⢵⡫⣞⣝⢞⢷⣹⡳⣝⢷⣫⢾⢮⣻⢵⡯⣻⣜⡽⣪⢗⠜⠄⠀⡀⠀⠀⡀⢀⠀⠐⠀⠀⠀⠁⠀⡀⠀⠀⠄⠠⠀⠀⠄⠠⠀⠀⠠⠀⠀⠀⡀⢀⠀⠀⡀⢀⠀⠀⠄⠠⠀⠀",
        "⠀⠐⠀⢀⠀⠀⡀⢀⠀⠈⠀⠀⠀⠁⢀⠀⠀⠀⢀⠀⠐⠀⠀⠄⠀⠀⠠⠀⠀⠈⠀⠀⠐⠀⠀⡀⠐⠀⢀⠀⠀⠀⠀⠈⠔⠀⠀⡈⠪⡪⣞⢼⡕⣽⢪⣗⡽⣪⢟⡼⣭⣫⢷⣝⢾⣝⣯⣳⢟⣷⣫⡷⣻⢵⡳⣝⣮⡫⠣⠀⠂⠀⢀⠀⠀⠀⠀⢀⠀⠁⠀⠠⠀⠀⠀⠄⠀⠀⡀⢀⠀⠀⡀⠀⠐⠀⠀⠄⠀⠀⠀⡀⠀⠀⠀⡀⠀⠀⡀⠀",
        "⠀⡀⠀⠀⡀⠀⠀⠀⠀⡀⠈⠀⠠⠀⠀⠀⠄⢀⠀⠀⡀⠀⠐⠀⠠⠀⠂⠐⠀⠀⠈⠀⡀⢀⠀⠀⠀⡀⠀⠀⠄⢀⠈⠀⠘⠠⠂⡀⡈⢘⢎⡞⣮⢳⣏⢾⣪⢏⡿⣜⣧⡻⣞⢮⡷⣳⢷⣝⡯⡾⣵⢫⣗⡽⣺⡵⣧⠫⠂⠀⡀⢀⠀⠀⠄⠀⠁⠀⠀⠀⠐⠀⢀⠀⠂⠐⠀⠀⠀⠀⢀⠀⠀⠈⠀⠀⠐⠀⠀⠠⠀⠀⠀⠠⠀⠀⡀⢀⠀⠈",
        "⠀⠀⠀⠄⠀⠈⠀⡀⠀⠀⡀⠀⠐⠀⠠⠀⠀⠀⡀⠀⠀⠈⠀⢀⠀⠀⡀⠀⠀⠁⢀⠀⠀⠀⠀⠠⠀⠀⠀⠄⠀⠀⠀⡀⠀⠁⠣⡘⡼⣲⢵⢝⡮⣳⢝⡮⣳⠯⡾⡵⣳⢽⣝⢯⢾⢝⠧⠫⠚⠁⠁⠳⢕⣽⡣⣿⡪⠁⠀⠀⠀⠀⠀⡀⠀⠈⠀⠀⠈⠀⡀⢀⠀⠀⡀⠀⠀⠁⠀⠠⠀⠀⠄⠀⠈⠀⡀⢀⠈⠀⠀⡀⠁⠀⡀⠀⠀⠀⠀⡀",
        "⡀⠀⠁⠀⡀⢀⠀⠀⡀⢀⠀⠈⠀⠀⠀⠂⠀⡀⠀⠈⠀⠀⠐⠀⠀⠀⠀⠈⠀⠀⠀⠀⠄⠀⠁⠀⡀⠀⠂⠐⠀⢀⠀⠀⡀⠀⠀⠑⠬⡺⣕⢟⡮⡷⣝⡽⡊⠛⠘⠉⠃⠁⠁⠁⠀⠀⠀⠀⠀⠀⠀⡀⠑⠛⠊⠀⠀⠀⠈⠀⢀⠀⠀⠀⡀⢀⠈⠀⡀⢀⠀⠀⠀⠀⠀⠈⠀⠀⠈⠀⠀⡀⠀⠈⠀⠀⠀⠀⠀⢀⠀⠀⡀⠀⠀⠈⠀⡀⠀⠀",
        "⠀⠈⠀⠀⠀⠀⠀⡀⠀⠀⠀⡀⠈⠀⡀⠠⠀⠀⡀⢀⠈⠀⡀⠀⠈⠀⠠⠀⠈⠀⠠⠀⠐⠀⢀⠀⠀⠐⠀⠀⠀⠀⡀⠀⠀⠈⠀⠀⠀⠑⢽⢝⣞⢟⣮⠃⠀⠀⠀⠀⠀⠐⠀⠀⠐⠀⢀⠀⠐⠀⠀⠄⠀⠐⠀⠀⠐⠀⢀⠀⠂⠀⢀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠁⠈⠀⠀⠈⠀⠠⠀⠀⠀⡀⢀⠈⠀⠠⠀⠁⠀⠀⡀⠀⠈⠀⢀⠀⠀⠄⢀",
        "⠀⠀⠈⠀⡀⠠⠀⠀⡀⢀⠀⠀⡀⠀⠀⠀⢀⠀⠀⠀⠀⠀⠀⠈⠀⠀⠐⠀⢀⠀⠐⠀⠀⡀⠀⠀⠄⠀⠀⠁⢀⠀⠀⠈⠀⠀⠈⠀⡀⠀⠀⠙⠪⠛⠂⠀⢀⠀⠀⠁⠀⢀⠀⠁⠀⡀⠀⠀⡀⠀⠐⠀⠀⡀⠀⠁⠀⢀⠀⠀⡀⠀⠀⠀⠄⠀⠁⠈⠀⠀⠈⠀⡀⠀⠈⠀⡀⠀⠐⠀⠠⠀⠀⠀⠀⡀⠀⠐⠀⢀⠀⠀⡀⠀⠐⠀⠀⡀⠀⠀",
        "⠁⠈⠀⠀⠀⠀⡀⠀⠀⠀⢀⠀⠀⠈⠀⢀⠀⠀⡀⠀⠁⠈⠀⠀⠈⠀⢀⠀⠀⠀⡀⢀⠀⠀⡀⢀⠀⠈⠀⢀⠀⠀⠠⠀⠈⠀⡀⢀⠀⠈⠀⠀⠀⠀⠀⠀⠀⠀⠄⠀⠁⠀⠀⢀⠀⠀⡀⢀⠀⠈⠀⢀⠀⠀⡀⠀⠂⠀⠀⡀⠀⠈⠀⠀⠐⠀⢀⠀⠈⠀⡀⢀⠀⠈⠀⠀⠀⠈⠀⢀⠀⠂⠀⠀⠂⠀⢀⠀⠀⠀⡀⠀⠀⠈⠀⠀⠀⠀⡀⠀",
        "⠀⢀⠀⠁⢀⠀⠀⠈⠀⢀⠀⠀⡀⠀⠂⠀⠀⡀⠀⠈⠀⠀⠀⠁⠀⡀⠀⠠⠀⡀⠀⠀⡀⠀⠀⠀⠀⠀⠂⠀⠀⠀⠂⠐⠀⠀⠀⠀⠀⠀⠐⠀⢀⠀⠈⠀⡀⢀⠀⠈⠀⠀⠐⠀⠀⠀⠀⠀⠀⡀⠐⠀⠀⠀⠀⠐⠀⠀⠠⠀⠀⡀⠈⠀⡀⠀⠀⠀⡀⢀⠀⠀⠀⠀⠈⠀⡀⠀⠂⠀⠀⠀⠈⠀⡀⠀⠀⠐⠀⢀⠀⠈⠀⠀⠈⠀⡀⢀⠀⠈",
        "⠂⠀⠀⢀⠀⠠⠀⠀⠂⠀⠀⡀⠀⠂⠀⡀⠠⠀⠀⡀⠈⠀⡀⠀⠂⠀⢀⠀⠀⠀⡀⠀⠀⠈⠀⠀⠁⠐⠀⠀⠁⠐⠀⠀⠀⠁⠀⡀⠁⠈⠀⠀⠀⠠⠀⢀⠀⠀⢀⠀⠈⠀⡀⠀⠈⠀⡀⠀⠂⠀⠀⠀⠈⠀⡀⢀⠀⠁⠀⢀⠀⠀⡀⠀⠀⠈⠀⢀⠀⠀⠀⠀⠁⠈⠀⠀⠀⠐⠀⠀⠈⠀⡀⠀⠀⠈⠀⠀⠐⠀⠀⠀⠈⠀⡀⢀⠀⠀⠀⠀",
        "⠀⠀⠂⠀⠀⠀⠀⠈⠀⠀⠠⠀⠀⡀⠀⠀⠀⢀⠀⠀⡀⠀⠀⠐⠀⠀⠀⠠⠀⢀⠀⠈⠀⠀⠈⠀⡀⢀⠀⠈⠀⠀⠀⠈⠀⠀⠠⠀⠀⡀⠀⠁⢀⠀⠀⠀⡀⠀⠀⠄⠀⡀⠀⠈⠀⠀⠀⠈⠀⠀⠈⠀⡀⢀⠀⠀⠀⠀⠂⠀⠀⡀⠀⠈⠀⠀⠂⠀⠀⠀⠁⠈⠀⠀⠀⠁⢀⠀⠀⠁⢀⠀⠀⠈⠀⠀⠈⠀⡀⠀⠀⠁⢀⠀⠀⠀⠀⠀⠁⠐"
    };

    private static final int PATTERN_SPLIT = 20; // Split point for left/right sides - sparse stars on left

    // get left side of ascii art for a line
    public static String getAsciiArtLeft(int lineIndex) {
        if (lineIndex >= 0 && lineIndex < ASCII_ART_PATTERN.length) {
            String line = ASCII_ART_PATTERN[lineIndex];
            return colored(line.substring(0, Math.min(PATTERN_SPLIT, line.length())), CYAN);
        }
        return " ".repeat(PATTERN_SPLIT);
    }

    // get right side of ascii art for a line
    public static String getAsciiArtRight(int lineIndex) {
        if (lineIndex >= 0 && lineIndex < ASCII_ART_PATTERN.length) {
            String line = ASCII_ART_PATTERN[lineIndex];
            if (line.length() > PATTERN_SPLIT) {
                return colored(line.substring(PATTERN_SPLIT), CYAN);
            }
        }
        return "";
    }

    // how many lines of ascii art we have
    public static int getAsciiArtLineCount() {
        return ASCII_ART_PATTERN.length;
    }

    // simple star pattern for left side (no fancy art)
    public static String getStarPatternLeft(int lineIndex) {
        // Return sparse star pattern matching the left side width
        return getStarFillLine(lineIndex).substring(0, Math.min(PATTERN_SPLIT, getStarFillLine(lineIndex).length()));
    }

    // simple star pattern for right side
    public static String getStarPatternRight(int lineIndex) {
        // Return sparse star pattern for the right side
        String starLine = getStarFillLine(lineIndex);
        int rightStart = PATTERN_SPLIT + 55 + 55; // left + content + padding
        if (starLine.length() > rightStart) {
            return starLine.substring(rightStart);
        }
        return "";
    }

    // pad a line to a specific width (for aligning with art)
    // truncates if too long
    public static String padToWidth(String line, int width) {
        // Strip ANSI codes to measure actual visible length
        String stripped = line.replaceAll("\033\\[[0-9;]+m", "");
        int visibleLength = stripped.length();
        
        if (visibleLength > width) {
            // Need to truncate - this is complex with ANSI codes
            // Build truncated string character by character, preserving ANSI codes
            StringBuilder result = new StringBuilder();
            int visibleCount = 0;
            int i = 0;
            while (i < line.length() && visibleCount < width) {
                if (line.charAt(i) == '\033') {
                    // ANSI escape sequence - copy it entirely
                    int end = line.indexOf('m', i);
                    if (end != -1) {
                        result.append(line, i, end + 1);
                        i = end + 1;
                    } else {
                        result.append(line.charAt(i));
                        i++;
                    }
                } else {
                    result.append(line.charAt(i));
                    visibleCount++;
                    i++;
                }
            }
            // Add reset code at end to clean up any open formatting
            result.append("\033[0m");
            return result.toString();
        }
        
        int padding = width - visibleLength;
        return line + " ".repeat(padding);
    }

    // decorative star patterns to fill empty space
    private static final String[] STAR_FILL_LINES = {
        "⠀⠈⠀⠐⠀⢀⠀⠈⠀⡀⢀⠀⠈⠀⠈⠀⢀⠀⠂⠀⠂⠀⢀⠀⡀⠀⠈⠀⡀⠀⢀⠀⠁⢀⠀⠈⠀⢀⠀⡀⠀⠂⠀⡀⠀⠈⠀⠀⠐⠀⢀⠀⠈⠀⡀⢀⠀⠈⠀⠈",
        "⠀⠁⠀⡀⠀⠠⠀⡀⠀⡀⠀⠂⠀⠂⠐⠀⠀⠐⠀⢀⠀⠈⠀⡀⢀⠀⠈⠀⠐⠀⡀⠐⠀⠠⠀⡀⠈⠀⡀⠀⡀⢀⠀⠈⠀⡀⠀⠂⠀⠈⠀⡀⠀⠐⠀⢀⠀⠈⠀⡀",
        "⠂⠀⢀⠀⠠⠀⠂⠀⡀⠀⠂⠀⡀⠠⠀⡀⠈⠀⡀⠀⠂⠀⢀⠀⡀⠀⠈⠀⢀⠀⠀⠀⡀⠀⠂⠀⡀⠠⠀⡀⠂⠀⢀⠀⠠⠀⠂⠀⡀⠀⠂⠀⡀⠠⠀⡀",
        "⠈⠀⡀⠀⠂⠀⢀⠀⡀⠀⠈⠀⠁⠐⠀⠁⠐⠀⠁⠀⡀⠁⠈⠀⠠⠀⢀⠀⢀⠀⠈⠀⡀⠀⠈⠀⡀⠀⠂⠀⠈⠀⡀⢀⠀⠁⠀⢀⠀⡀⠀⠈⠀⢀⠀⠁⠈⠀⠐",
        "⠂⠀⠀⠈⠀⠠⠀⡀⠀⠀⢀⠀⠈⠀⡀⠀⠂⠀⢀⠀⠀⡀⠀⠈⠀⢀⠀⠀⡀⠀⠂⠀⢀⠀⡀⠀⠂⠀⠀⠈⠀⠠⠀⡀⠀⠀⢀⠀⠈⠀⡀⠀⠂⠀⢀",
        "⡀⠀⠐⠀⠀⠠⠀⢀⠀⠈⠀⠈⠀⡀⢀⠀⠈⠀⠈⠀⠠⠀⡀⠀⠁⢀⠀⡀⠀⠄⠀⡀⠀⠈⠀⠈⠀⠈⠀⡀⢀⠀⠀⠂⠀⡀⠀⠈⠀⠂⠀⠁⠈⠀⠁⢀⠀⠁⢀",
        "⠀⠈⠀⠐⠀⢀⠀⠈⠀⡀⢀⠀⠈⠀⠈⠀⢀⠀⠂⠀⠂⠀⢀⠀⡀⠀⠈⠀⡀⠀⢀⠀⠁⢀⠀⠈⠀⢀⠀⡀⠀⠂⠀⡀⠀⠈⠀⠈⠀⠐⠀⢀⠀⠈⠀⡀⢀⠀⠈",
        "⠀⠁⠀⡀⠀⠠⠀⡀⠀⡀⠀⠂⠀⠂⠐⠀⠀⠐⠀⢀⠀⠈⠀⡀⢀⠀⠈⠀⠐⠀⡀⠐⠀⠠⠀⡀⠈⠀⡀⠀⡀⢀⠀⠈⠀⡀⠀⠂⠀⠈⠀⡀⠀⠐⠀⢀⠀⠈⠀⡀",
        "⠂⠀⢀⠀⠠⠀⠂⠀⡀⠀⠂⠀⡀⠠⠀⡀⠂⠀⢀⠀⠠⠀⠂⠀⡀⠀⠂⠀⡀⠠⠀⡀⠂⠀⢀⠀⠂⠀⢀⠀⠠⠀⠂⠀⡀⠀⠂⠀⡀⠠⠀⡀⠂⠀⢀",
        "⠈⠀⡀⠀⠂⠀⢀⠀⡀⠀⠈⠀⠁⠐⠀⠁⠐⠀⠁⠀⡀⠁⠈⠀⠠⠀⢀⠀⢀⠀⠈⠀⡀⠀⠈⠀⡀⠀⠂⠀⠈⠀⡀⢀⠀⠁⠀⢀⠀⡀⠀⠈⠀⢀⠀⠁⠈⠀⠐",
        "⠂⠀⠀⠈⠀⠠⠀⡀⠀⠀⢀⠀⠂⠀⠀⠈⠀⠠⠀⡀⠀⠀⢀⠀⠂⠀⠀⠈⠀⠠⠀⡀⠀⠂⠀⠀⠈⠀⠠⠀⡀⠀⠀⢀⠀⠂⠀⠀⠈⠀⠠⠀⡀⠀⠀⢀",
        "⡀⠀⠐⠀⠀⠠⠀⢀⠀⠈⠀⠈⠀⡀⢀⠀⠈⠀⠈⠀⠠⠀⡀⠀⠁⢀⠀⡀⠀⠄⠀⡀⠀⠈⠀⠈⠀⠈⠀⡀⢀⠀⠀⠂⠀⡀⠀⠈⠀⠂⠀⠁⠈⠀⡀⠀⠐⠀⠀",
        "⠀⠈⠀⠐⠀⢀⠀⠈⠀⡀⢀⠀⠈⠀⠈⠀⢀⠀⠂⠀⠂⠀⢀⠀⡀⠀⠈⠀⡀⠀⢀⠀⠁⢀⠀⠈⠀⢀⠀⡀⠀⠂⠀⡀⠀⠈⠀⠈⠀⠐⠀⢀⠀⠈⠀⡀⢀⠀⠈",
        "⠀⠁⠀⡀⠀⠠⠀⡀⠀⡀⠀⠂⠀⠂⠐⠀⠀⠐⠀⢀⠀⠈⠀⡀⢀⠀⠈⠀⠐⠀⡀⠐⠀⠠⠀⡀⠈⠀⡀⠀⡀⢀⠀⠈⠀⡀⠀⠂⠀⠈⠀⡀⠀⠐⠀⢀⠀⠈⠀⡀"
    };
    
    private static final int CONTENT_WIDTH = 55;
    private static final int STAR_FILL_OFFSET = 6; // offset to align with menu content

    // get a full line of star fill pattern
    public static String getStarFillLine(int index) {
        String pattern = STAR_FILL_LINES[index % STAR_FILL_LINES.length];
        // Ensure we have enough stars to fill the width
        String fullPattern = pattern;
        while (fullPattern.length() < CONTENT_WIDTH) {
            fullPattern = fullPattern + pattern;
        }
        return colored(fullPattern.substring(0, CONTENT_WIDTH), CYAN);
    }

    // wrap content with stars on left and right
    // makes the ui look pretty
    public static String wrapContentWithStars(String line, int width, int lineIndex) {
        String pattern = STAR_FILL_LINES[lineIndex % STAR_FILL_LINES.length];
        
        // Strip ANSI codes to measure actual visible length
        String stripped = line.replaceAll("\033\\[[0-9;]+m", "");
        int contentLength = stripped.length();
        
        // Calculate maximum content length (width minus the left star offset)
        int maxContentLength = width - STAR_FILL_OFFSET;
        
        // If content is too long, we can't add right stars - just pad to width
        if (contentLength >= maxContentLength) {
            // Content fills or exceeds available space - just use padToWidth
            return padToWidth(line, width);
        }
        
        // Ensure pattern is long enough by repeating it
        String extendedPattern = pattern;
        while (extendedPattern.length() < width * 2) {
            extendedPattern = extendedPattern + pattern;
        }
        
        // Left side: stars for the offset area (first 6 characters of pattern)
        String leftStars = extendedPattern.substring(0, STAR_FILL_OFFSET);
        
        // Right side: stars to fill remaining width after content
        int usedWidth = STAR_FILL_OFFSET + contentLength;
        int rightPadding = Math.max(0, width - usedWidth);
        String rightStars = "";
        if (rightPadding > 0) {
            // Continue the pattern from where we left off (after offset + content position)
            int startPos = STAR_FILL_OFFSET + contentLength;
            rightStars = extendedPattern.substring(startPos, startPos + rightPadding);
        }
        
        return colored(leftStars, CYAN) + line + colored(rightStars, CYAN);
    }

    // adds star lines to fill remaining space
    // note: ScreenBuffer.printWithArt() handles this now
    public static void addStarBufferLines(ScreenBuffer buffer) {
        // star fill handled by ScreenBuffer.printWithArt()
    }

    // dont instantiate this class
    private ConsoleUtils() {
    }
}
