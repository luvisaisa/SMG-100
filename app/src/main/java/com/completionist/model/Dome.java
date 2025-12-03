package com.completionist.model;

import com.completionist.progress.GameProgress;
import java.util.List;

// domes are like the world areas - they contain galaxies
// terrace, fountain, kitchen, etc.
public class Dome implements ICompletionTrackable {
    private final String id;
    private final String name;
    private final List<Galaxy> galaxies;
    private Game game;
    private UnlockCondition unlockCondition;

    public Dome(String id, String name, List<Galaxy> galaxies) {
        this.id = id;
        this.name = name;
        this.galaxies = List.copyOf(galaxies);  // immutable

        // link each galaxy back to this dome
        for (Galaxy galaxy : this.galaxies) {
            galaxy.setDome(this);
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Galaxy> getGalaxies() {
        return galaxies;  // Already immutable
    }

    public Game getGame() {
        return game;
    }

    // links dome to parent game (internal use)
    void setGame(Game game) {
        this.game = game;
    }

    public UnlockCondition getUnlockCondition() {
        return unlockCondition;
    }

    public void setUnlockCondition(UnlockCondition unlockCondition) {
        this.unlockCondition = unlockCondition;
    }

    // is this dome accessible yet?
    public boolean isUnlocked(GameProgress progress) {
        if (unlockCondition == null) {
            return true;
        }
        return unlockCondition.isMet(progress);
    }

    // find a galaxy by id
    public Galaxy getGalaxyById(String galaxyId) {
        return galaxies.stream()
                .filter(g -> g.getId().equals(galaxyId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public int getTotalItems() {
        return galaxies.stream()
                .mapToInt(Galaxy::getTotalItems)
                .sum();
    }

    @Override
    public int getCompletedItems(GameProgress gameProgress) {
        return galaxies.stream()
                .mapToInt(g -> g.getCompletedItems(gameProgress))
                .sum();
    }

    @Override
    public String toString() {
        return String.format("%s (%d galaxies, %d total stars)", name, galaxies.size(), getTotalItems());
    }
}
