package com.completionist.model;

// interface for checking if something can be unlocked
// like "collect all main stars" or "get 15 total stars"
public interface UnlockCondition {
    // returns true if the condition is satisfied
    boolean isMet(Object context);

    // human readable description of what needs to happen
    default String getDescription() {
        return "Unknown unlock condition";
    }
}
