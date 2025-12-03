package com.completionist.model;

import com.completionist.progress.GameProgress;

// unlocks when you've collected every main star in a galaxy
// used for comet stars that appear after you beat all main missions
public class AllMainStarsInGalaxyCondition implements UnlockCondition {
    private final Galaxy galaxy;

    public AllMainStarsInGalaxyCondition(Galaxy galaxy) {
        this.galaxy = galaxy;
    }

    @Override
    public boolean isMet(Object context) {
        if (!(context instanceof GameProgress)) {
            return false;
        }

        GameProgress progress = (GameProgress) context;

        // loop through and check each main star
        for (Star star : galaxy.getStars()) {
            if (star instanceof MainStar) {
                if (!progress.isStarCollected(star.getId())) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public String getDescription() {
        return "Collect all Main stars in " + galaxy.getName();
    }
}
