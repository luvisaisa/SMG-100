package com.completionist.model;

import com.completionist.progress.GameProgress;

// anything that can track completion progress
// games, domes, and galaxies all implement this
public interface ICompletionTrackable {
    // total stars/items in this thing
    int getTotalItems();

    // how many are completed based on player progress
    int getCompletedItems(GameProgress gameProgress);

    // calculate completion percentage (0-100)
    default double getCompletionPercentage(GameProgress gameProgress) {
        int total = getTotalItems();
        if (total == 0) {
            return 0.0;
        }
        return (getCompletedItems(gameProgress) * 100.0) / total;
    }
}
