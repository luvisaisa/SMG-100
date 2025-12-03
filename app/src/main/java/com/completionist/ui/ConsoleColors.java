package com.completionist.ui;

// ansi color codes for the console ui
// going for a purple/blue midnight theme
public class ConsoleColors {
    // basic colors
    public static final String RESET = "\033[0m";
    public static final String BLACK = "\033[30m";
    public static final String WHITE = "\033[37m";

    // the main theme colors
    public static final String PURPLE = "\033[35m";           // Magenta - stars, checkmarks
    public static final String BRIGHT_PURPLE = "\033[95m";    // Bright Magenta - highlights
    public static final String BLUE = "\033[34m";             // Blue - names, data
    public static final String BRIGHT_BLUE = "\033[94m";      // Bright Cyan - cursor, prompts
    public static final String CYAN = "\033[36m";             // Cyan - decorations
    public static final String BRIGHT_CYAN = "\033[96m";      // Bright Cyan - active items

    // grays
    public static final String DARK_GRAY = "\033[90m";        // Dim - locked, empty progress
    public static final String LIGHT_GRAY = "\033[37m";       // Regular text

    // yellow for requirements
    public static final String YELLOW = "\033[33m";           // Yellow - unlock requirements
    public static final String BRIGHT_YELLOW = "\033[93m";    // Bright Yellow - highlighted requirements

    // text styles
    public static final String BOLD = "\033[1m";
    public static final String DIM = "\033[2m";
    public static final String ITALIC = "\033[3m";
    public static final String UNDERLINE = "\033[4m";

    // semantic colors - what each thing looks like
    public static final String TITLE = CYAN;                  // ★ decorations
    public static final String DOME = BRIGHT_PURPLE;          // ⌂ dome names
    public static final String GALAXY = BLUE;                 // ꩜ galaxy names
    public static final String STAR_COLLECTED = PURPLE;       // ⭑ collected stars
    public static final String STAR_UNCOLLECTED = DARK_GRAY;  // ☆ uncollected stars
    public static final String PROGRESS_FILLED = PURPLE;      // ✦ filled progress
    public static final String PROGRESS_EMPTY = DARK_GRAY;    // ✧ empty progress
    public static final String DIVIDER = CYAN;                // ─── ⋆⋅☆⋅⋆ ───
    public static final String PROMPT = BRIGHT_BLUE;          // > _ cursor
    public static final String LOCKED = DARK_GRAY;            // ⏾ locked stars
    public static final String HIDDEN = DARK_GRAY;            // ??? hidden stars
    public static final String NOTE = LIGHT_GRAY;             // note text
    public static final String MENU_OPTION = WHITE;           // menu items

    // luigi mode colors
    public static final String GREEN = "\033[32m";            // Green - Luigi theme
    public static final String BRIGHT_GREEN = "\033[92m";     // Bright Green - Luigi highlights
    public static final String LUIGI_THEME = GREEN;           // Luigi theme color (like CYAN for Mario)
    public static final String LUIGI_STAR = GREEN;            // Luigi star collected
    public static final String LUIGI_PROGRESS = GREEN;        // Luigi progress filled

    // star type colors (when collected)
    public static final String RED = "\033[31m";              // Red - Comet stars when collected
    public static final String BRIGHT_RED = "\033[91m";       // Bright Red - Comet star highlight
    public static final String COMET_STAR = RED;              // Comet star collected color
    public static final String GREEN_STAR = BRIGHT_GREEN;     // Green Power Star collected color

    // wrap text in a color and auto-reset
    public static String colored(String text, String color) {
        return color + text + RESET;
    }

    // color without reset (for chaining)
    public static String coloredNoReset(String text, String color) {
        return color + text;
    }

    private ConsoleColors() {
        // don't instantiate
    }
}
