package com.completionist.model;

import com.completionist.progress.GameProgress;
import java.util.List;

// the whole game - contains everything
// game -> domes -> galaxies -> stars
public class Game implements ICompletionTrackable {
    private final String id;
    private final String name;
    private final List<Dome> domes;

    public Game(String id, String name, List<Dome> domes) {
        this.id = id;
        this.name = name;
        this.domes = List.copyOf(domes);  // immutable

        // link each dome to this game
        for (Dome dome : this.domes) {
            dome.setGame(this);
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Dome> getDomes() {
        return domes;  // Already immutable
    }

    // find a dome by id
    public Dome getDomeById(String domeId) {
        return domes.stream()
                .filter(d -> d.getId().equals(domeId))
                .findFirst()
                .orElse(null);
    }

    // search for a star anywhere in the game
    public Star findStarById(String starId) {
        for (Dome dome : domes) {
            for (Galaxy galaxy : dome.getGalaxies()) {
                Star star = galaxy.getStarById(starId);
                if (star != null) {
                    return star;
                }
            }
        }
        return null;
    }

    // find which galaxy contains a star
    public Galaxy findGalaxyByStarId(String starId) {
        for (Dome dome : domes) {
            for (Galaxy galaxy : dome.getGalaxies()) {
                if (galaxy.getStarById(starId) != null) {
                    return galaxy;
                }
            }
        }
        return null;
    }

    @Override
    public int getTotalItems() {
        return domes.stream()
                .mapToInt(Dome::getTotalItems)
                .sum();
    }

    @Override
    public int getCompletedItems(GameProgress gameProgress) {
        return domes.stream()
                .mapToInt(d -> d.getCompletedItems(gameProgress))
                .sum();
    }

    @Override
    public String toString() {
        return String.format("%s (%d domes, %d total stars)", name, domes.size(), getTotalItems());
    }
}
