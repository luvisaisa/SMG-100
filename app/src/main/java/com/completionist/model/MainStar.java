package com.completionist.model;

// regular mission stars - the main objectives
public class MainStar extends Star {

    public MainStar(String id, String name) {
        super(id, name, false, null);
    }

    @Override
    public String getTypeIcon() {
        return "⭐";
    }
}
