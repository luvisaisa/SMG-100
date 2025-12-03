package com.completionist.model;

import com.completionist.progress.GameProgress;
import java.util.List;

// a galaxy holds a bunch of stars
// these get grouped into domes
public class Galaxy implements ICompletionTrackable {
    private final String id;
    private final String name;
    private final List<Star> stars;
    private final UnlockCondition unlockCondition;
    private Dome dome;

    public Galaxy(String id, String name, List<Star> stars, UnlockCondition unlockCondition) {
        this.id = id;
        this.name = name;
        this.stars = List.copyOf(stars);  // make it immutable
        this.unlockCondition = unlockCondition;
    }

    public Galaxy(String id, String name, List<Star> stars) {
        this(id, name, stars, null);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Star> getStars() {
        return stars;  // Already immutable
    }

    public Dome getDome() {
        return dome;
    }

    public UnlockCondition getUnlockCondition() {
        return unlockCondition;
    }

    // checks if player can access this galaxy
    public boolean isUnlocked(GameProgress progress) {
        return unlockCondition == null || unlockCondition.isMet(progress);
    }

    // links this galaxy to its parent dome (called internally)
    void setDome(Dome dome) {
        this.dome = dome;
    }

    // find a star in this galaxy by id
    public Star getStarById(String starId) {
        return stars.stream()
                .filter(s -> s.getId().equals(starId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public int getTotalItems() {
        return stars.size();
    }

    @Override
    public int getCompletedItems(GameProgress gameProgress) {
        if (gameProgress == null) {
            return 0;
        }
        return (int) stars.stream()
                .filter(star -> gameProgress.isStarCollected(star.getId()))
                .count();
    }

    @Override
    public String toString() {
        return String.format("%s (%d stars)", name, stars.size());
    }
}
