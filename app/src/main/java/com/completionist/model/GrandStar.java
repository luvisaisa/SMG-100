package com.completionist.model;

// grand stars - big boss completion rewards
public class GrandStar extends Star {

    public GrandStar(String id, String name) {
        super(id, name, false, null);
    }

    @Override
    public String getTypeIcon() {
        return "🌠";
    }
}
