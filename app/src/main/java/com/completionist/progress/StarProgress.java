package com.completionist.progress;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

// tracks progress on a single star
// collected status, notes, difficulty rating, etc
public class StarProgress {
    private final String starId;
    private boolean collected;
    private LocalDateTime collectedAt;
    private boolean revealed;  // for hidden stars you've found
    private String note;
    private Integer difficultyRating;  // 1-5, null if not rated

    @JsonCreator
    public StarProgress(
            @JsonProperty("starId") String starId,
            @JsonProperty("collected") boolean collected,
            @JsonProperty("collectedAt") LocalDateTime collectedAt,
            @JsonProperty("revealed") boolean revealed,
            @JsonProperty("note") String note,
            @JsonProperty("difficultyRating") Integer difficultyRating) {
        this.starId = starId;
        this.collected = collected;
        this.collectedAt = collectedAt;
        this.revealed = revealed;
        this.note = note;
        this.difficultyRating = difficultyRating;
    }

    public StarProgress(String starId) {
        this(starId, false, null, false, null, null);
    }

    public String getStarId() {
        return starId;
    }

    public boolean isCollected() {
        return collected;
    }

    public LocalDateTime getCollectedAt() {
        return collectedAt;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public String getNote() {
        return note;
    }

    public Integer getDifficultyRating() {
        return difficultyRating;
    }

    // mark as collected with current timestamp
    public void markCollected() {
        this.collected = true;
        this.collectedAt = LocalDateTime.now();
    }

    // undo collection
    public void markUncollected() {
        this.collected = false;
        this.collectedAt = null;
    }

    // reveal a hidden star
    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    // add a note about this star
    public void setNote(String note) {
        this.note = note;
    }

    // set difficulty 1-5 (or null to clear)
    public void setDifficultyRating(Integer rating) {
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException("Difficulty rating must be 1-5 or null");
        }
        this.difficultyRating = rating;
    }

    // shows stars like ★★★☆☆
    public String getDifficultyStars() {
        if (difficultyRating == null) {
            return "Not rated";
        }
        return "★".repeat(difficultyRating) + "☆".repeat(5 - difficultyRating);
    }

    @Override
    public String toString() {
        String status = collected ? "✓" : " ";
        String revealedInfo = revealed ? " (revealed)" : "";
        return String.format("[%s] %s%s", status, starId, revealedInfo);
    }
}
