package com.completionist.model;

// mario or luigi mode
// the game has 242 total stars:
// - 120 for mario (first playthrough) + 1 bonus (grand finale)
// - 120 for luigi (unlocked after mario's 120) + 1 bonus
public enum CharacterMode {
    MARIO("mario", "Mario", "\033[35m"),      // purple theme
    LUIGI("luigi", "Luigi", "\033[32m");       // green theme

    private final String id;
    private final String displayName;
    private final String themeColor;

    CharacterMode(String id, String displayName, String themeColor) {
        this.id = id;
        this.displayName = displayName;
        this.themeColor = themeColor;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getThemeColor() {
        return themeColor;
    }

    // how many stars needed to unlock this mode
    public int getUnlockRequirement() {
        return switch (this) {
            case MARIO -> 0;  // always available
            case LUIGI -> 120;  // need all mario stars
        };
    }

    // prefix for star ids in this mode
    public String getStarPrefix() {
        return switch (this) {
            case MARIO -> "";  // no prefix
            case LUIGI -> "luigi-";  // luigi stars have prefix
        };
    }
}
