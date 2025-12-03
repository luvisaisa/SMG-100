package com.completionist.model;

// 3 green stars that unlock planet of trials
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
