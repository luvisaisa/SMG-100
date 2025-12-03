package com.completionist.model;

import com.completionist.progress.GameProgress;
import java.util.List;

// prankster comet unlock logic
// you need at least 13 total stars AND all the main stars in the galaxy
// based on how the actual game works
public class CometUnlockCondition implements UnlockCondition {
    private static final int MINIMUM_STARS_FOR_COMETS = 13;
    private final List<String> mainStarIds;

    public CometUnlockCondition(String... mainStarIds) {
        this.mainStarIds = List.of(mainStarIds);
    }

    public CometUnlockCondition(List<String> mainStarIds) {
        this.mainStarIds = List.copyOf(mainStarIds);
    }

    @Override
    public boolean isMet(Object context) {
        if (!(context instanceof GameProgress)) {
            return false;
        }

        GameProgress progress = (GameProgress) context;

        // need 13 stars minimum
        if (progress.getCollectedCount() < MINIMUM_STARS_FOR_COMETS) {
            return false;
        }

        // also need all main stars from this galaxy
        for (String starId : mainStarIds) {
            if (!progress.isStarCollected(starId)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String getDescription() {
        return String.format("Collect %d+ total stars and all main stars in this galaxy", MINIMUM_STARS_FOR_COMETS);
    }

    // the magic number for comets
    public static int getMinimumStarsRequired() {
        return MINIMUM_STARS_FOR_COMETS;
    }

    // which main stars need to be collected
    public List<String> getMainStarIds() {
        return mainStarIds;
    }
}
