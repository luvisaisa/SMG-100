package com.completionist.model;

// base class for all the different star types
// stars are the things you collect in each galaxy
public abstract class Star {
    private final String id;
    private final String name;
    private final boolean hiddenByDefault;
    private final UnlockCondition unlockCondition;

    // set up a new star with all its properties
    protected Star(String id, String name, boolean hiddenByDefault, UnlockCondition unlockCondition) {
        this.id = id;
        this.name = name;
        this.hiddenByDefault = hiddenByDefault;
        this.unlockCondition = unlockCondition;
    }

    // gets the star id
    public String getId() {
        return id;
    }

    // gets the display name
    public String getName() {
        return name;
    }

    // is this a secret star that starts hidden?
    public boolean isHiddenByDefault() {
        return hiddenByDefault;
    }

    // get the unlock condition (or null if always available)
    public UnlockCondition getUnlockCondition() {
        return unlockCondition;
    }

    // subclasses implement this to show the right emoji
    public abstract String getTypeIcon();

    @Override
    public String toString() {
        return String.format("%s %s [%s]", getTypeIcon(), name, id);
    }
}
