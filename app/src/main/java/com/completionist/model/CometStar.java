package com.completionist.model;

// comet challenge stars (speed runs, etc)
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
