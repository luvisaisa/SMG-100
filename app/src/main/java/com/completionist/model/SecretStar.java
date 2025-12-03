package com.completionist.model;

// secret bonus stars - hidden by default until you find them
public class SecretStar extends Star {

    public SecretStar(String id, String name, UnlockCondition unlockCondition) {
        super(id, name, true, unlockCondition);
    }

    public SecretStar(String id, String name) {
        this(id, name, null);
    }

    @Override
    public String getTypeIcon() {
        return "🌟";
    }
}
