package com.completionist.model;

// green power stars - hidden collectibles that unlock planet of trials
// there are 3 of them: battlerock, dusty dune, and buoy base
public class GreenStar extends Star {

    public GreenStar(String id, String name, UnlockCondition unlockCondition) {
        super(id, name, false, unlockCondition);  // not hidden, just hard to find
    }

    public GreenStar(String id, String name) {
        this(id, name, null);
    }

    @Override
    public String getTypeIcon() {
        return "💚";
    }
}
