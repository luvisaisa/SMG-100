package com.completionist.model;

import com.completionist.progress.GameProgress;

// needs X total stars to unlock
// used for galaxies that require a star threshold
public class TotalStarsCondition implements UnlockCondition {
    private final int requiredStars;

    public TotalStarsCondition(int requiredStars) {
        this.requiredStars = requiredStars;
    }

    @Override
    public boolean isMet(Object context) {
        if (!(context instanceof GameProgress)) {
            return false;
        }

        GameProgress progress = (GameProgress) context;
        // just count up all collected stars
        return progress.getCollectedCount() >= requiredStars;
    }

    @Override
    public String getDescription() {
        return "Collect " + requiredStars + " Power Stars";
    }

    public int getRequiredStars() {
        return requiredStars;
    }
}
