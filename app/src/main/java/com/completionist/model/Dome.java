package com.completionist.model;

import com.completionist.progress.GameProgress;
import java.util.List;

// terrace, fountain, kitchen etc - each has galaxies
public class Dome implements ICompletionTrackable {
    private final String id;
    private final String name;
    private final List<Galaxy> galaxies;
    private Game game;
    private UnlockCondition unlockCondition;

    public Dome(String id, String name, List<Galaxy> galaxies) {
        this.id = id;
        this.name = name;
        this.galaxies = List.copyOf(galaxies);
        for (Galaxy galaxy : this.galaxies) galaxy.setDome(this);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<Galaxy> getGalaxies() { return galaxies; }
    public Game getGame() { return game; }
    void setGame(Game game) { this.game = game; }
    public UnlockCondition getUnlockCondition() { return unlockCondition; }
    public void setUnlockCondition(UnlockCondition uc) { this.unlockCondition = uc; }

    public boolean isUnlocked(GameProgress progress) {
        return unlockCondition == null || unlockCondition.isMet(progress);
    }

    public Galaxy getGalaxyById(String galaxyId) {
        return galaxies.stream().filter(g -> g.getId().equals(galaxyId)).findFirst().orElse(null);
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
