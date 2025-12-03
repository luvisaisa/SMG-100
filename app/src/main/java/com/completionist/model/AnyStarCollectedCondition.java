package com.completionist.model;

import com.completionist.progress.GameProgress;
import java.util.List;

// unlocks if you've collected ANY of the specified stars
// like "get any main star to reveal comet"
public class AnyStarCollectedCondition implements UnlockCondition {
    private final List<String> starIds;

    public AnyStarCollectedCondition(String... starIds) {
        this.starIds = List.of(starIds);
    }

    public AnyStarCollectedCondition(List<String> starIds) {
        this.starIds = List.copyOf(starIds);
    }

    @Override
    public boolean isMet(Object context) {
        if (!(context instanceof GameProgress)) {
            return false;
        }

        GameProgress progress = (GameProgress) context;

        // just need one of them
        for (String starId : starIds) {
            if (progress.isStarCollected(starId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "Collect a main star";
    }
}
