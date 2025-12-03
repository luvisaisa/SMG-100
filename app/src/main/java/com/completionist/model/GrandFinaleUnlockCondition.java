package com.completionist.model;

import com.completionist.progress.GameProgress;

// the big one - grand finale galaxy unlock
// need 240 stars total (mario + luigi combined, no green stars or grand finale)
// plus all 6 green stars (3 mario, 3 luigi)
// this was tricky to get right
public class GrandFinaleUnlockCondition implements UnlockCondition {
    private static final int REQUIRED_TOTAL_STARS = 240;
    private static final int REQUIRED_GREEN_STARS = 6;

    @Override
    public boolean isMet(Object context) {
        if (!(context instanceof GameProgress)) {
            return false;
        }

        GameProgress progress = (GameProgress) context;
        
        // count regular stars (exclude grand finale and green stars)
        int totalStars = (int) progress.getAllStarProgress().values().stream()
                .filter(sp -> !sp.getStarId().equals("grand-finale-star-festival") &&
                              !sp.getStarId().equals("luigi-grand-finale-star-festival") &&
                              !sp.getStarId().contains("-green-star") &&
                              sp.isCollected())
                .count();

        // count green stars separately
        int greenStars = (int) progress.getAllStarProgress().values().stream()
                .filter(sp -> sp.getStarId().contains("-green-star") && sp.isCollected())
                .count();

        return totalStars >= REQUIRED_TOTAL_STARS && greenStars >= REQUIRED_GREEN_STARS;
    }

    // mode-aware check for ui display
    public boolean isMetForMode(GameProgress progress, CharacterMode mode) {
        // grand finale is global - same check either way
        return isMet(progress);
    }

    @Override
    public String getDescription() {
        return "Collect 240 Power Stars total (Mario + Luigi) plus all 6 Green Stars";
    }

    public static int getRequiredTotalStars() {
        return REQUIRED_TOTAL_STARS;
    }
}
