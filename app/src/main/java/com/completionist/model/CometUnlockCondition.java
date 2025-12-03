package com.completionist.model;

import com.completionist.progress.GameProgress;
import java.util.List;

// comets start appearing after 13 stars (looked this up on the wiki)
// also need all main missions done in that galaxy first
public class CometUnlockCondition implements UnlockCondition {
    private static final int MIN_STARS = 13;
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

        if (progress.getCollectedCount() < MIN_STARS) {
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
        return "Collect " + MIN_STARS + "+ stars and beat all main missions";
    }

    public static int getMinimumStarsRequired() { return MIN_STARS; }
    public List<String> getMainStarIds() { return mainStarIds; }
}
