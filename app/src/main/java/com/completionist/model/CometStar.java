package com.completionist.model;

// prankster comet stars - harder versions of regular missions
// these are hidden until unlock conditions are met
public class CometStar extends Star {

    public CometStar(String id, String name, UnlockCondition unlockCondition) {
        super(id, name, true, unlockCondition);
    }

    public CometStar(String id, String name) {
        this(id, name, null);
    }

    @Override
    public String getTypeIcon() {
        return "☄️";
    }
}
