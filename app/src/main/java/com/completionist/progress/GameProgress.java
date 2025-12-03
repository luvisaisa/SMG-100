package com.completionist.progress;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

// star collection progress for one game
public class GameProgress {
    private final String gameId;
    private final Map<String, StarProgress> starProgress;
    private final LocalDateTime startedAt;
    private LocalDateTime lastUpdated;

    @JsonCreator
    public GameProgress(
            @JsonProperty("gameId") String gameId,
            @JsonProperty("allStarProgress") Map<String, StarProgress> starProgress,
            @JsonProperty("startedAt") LocalDateTime startedAt,
            @JsonProperty("lastUpdated") LocalDateTime lastUpdated) {
        this.gameId = gameId;
        this.starProgress = starProgress != null ? new HashMap<>(starProgress) : new HashMap<>();
        this.startedAt = startedAt != null ? startedAt : LocalDateTime.now();
        this.lastUpdated = lastUpdated != null ? lastUpdated : LocalDateTime.now();
    }

    public GameProgress(String gameId) {
        this(gameId, null, null, null);
    }

    public String getGameId() {
        return gameId;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    // get star progress, creates it if doesn't exist (lazy)
    public StarProgress getStarProgress(String starId) {
        return starProgress.computeIfAbsent(starId, StarProgress::new);
    }

    // quick check if a star is collected
    public boolean isStarCollected(String starId) {
        StarProgress progress = starProgress.get(starId);
        return progress != null && progress.isCollected();
    }

    // check if a hidden star has been revealed
    public boolean isStarRevealed(String starId) {
        StarProgress progress = starProgress.get(starId);
        return progress != null && progress.isRevealed();
    }

    // total stars collected in this game
    public int getCollectedCount() {
        return (int) starProgress.values().stream()
                .filter(StarProgress::isCollected)
                .count();
    }

    // get all notes for stars that have them
    public Map<String, String> getAllNotes() {
        return starProgress.values().stream()
                .filter(sp -> sp.getNote() != null && !sp.getNote().isEmpty())
                .collect(Collectors.toMap(
                        StarProgress::getStarId,
                        StarProgress::getNote
                ));
    }

    // update the timestamp when something changes
    public void touch() {
        this.lastUpdated = LocalDateTime.now();
    }

    // get all star progress for saving
    public Map<String, StarProgress> getAllStarProgress() {
        return new HashMap<>(starProgress);
    }

    @Override
    public String toString() {
        return String.format("GameProgress[%s] - %d stars collected",
                gameId, getCollectedCount());
    }
}
