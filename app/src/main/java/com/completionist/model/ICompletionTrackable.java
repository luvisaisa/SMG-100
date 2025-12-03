package com.completionist.model;

import com.completionist.progress.GameProgress;

// games, domes, galaxies - anything with stars to collect
public interface ICompletionTrackable {
    int getTotalItems();
    int getCompletedItems(GameProgress gp);

    default double getCompletionPercentage(GameProgress gp) {
        int total = getTotalItems();
        if (total == 0) return 0.0;
        return (getCompletedItems(gp) * 100.0) / total;
    }
}
