package com.completionist.model;

public interface UnlockCondition {
    boolean isMet(Object context);
    default String getDescription() { return "???"; }
}
