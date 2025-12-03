package com.completionist.model;

import com.completionist.progress.GameProgress;
import java.util.List;

// holds a list of stars
public class Galaxy implements ICompletionTrackable {
    private final String id;
    private final String name;
    private final List<Star> stars;
    private final UnlockCondition unlockCondition;
    private Dome dome;

    public Galaxy(String id, String name, List<Star> stars, UnlockCondition unlockCondition) {
        this.id = id;
        this.name = name;
        this.stars = List.copyOf(stars);
        this.unlockCondition = unlockCondition;
    }

    public Galaxy(String id, String name, List<Star> stars) {
        this(id, name, stars, null);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<Star> getStars() { return stars; }
    public Dome getDome() { return dome; }
    public UnlockCondition getUnlockCondition() { return unlockCondition; }
    void setDome(Dome dome) { this.dome = dome; }

    public boolean isUnlocked(GameProgress progress) {
        return unlockCondition == null || unlockCondition.isMet(progress);
    }

    public Star getStarById(String starId) {
        return stars.stream().filter(s -> s.getId().equals(starId)).findFirst().orElse(null);
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
