package com.completionist.progress;

import com.completionist.model.CharacterMode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

// save file - has progress for all games
public class PlayerProfile {
    private final String playerId;
    private String displayName;
    private final Map<String, GameProgress> gameProgress;
    private final LocalDateTime createdAt;
    private LocalDateTime lastPlayed;
    private boolean spoilersEnabled;  // show all hidden stars
    private int starBits;  // currency
    private long playTimeMinutes;
    private CharacterMode characterMode;  // mario or luigi

    @JsonCreator
    public PlayerProfile(
            @JsonProperty("playerId") String playerId,
            @JsonProperty("displayName") String displayName,
            @JsonProperty("allGameProgress") Map<String, GameProgress> gameProgress,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("lastPlayed") LocalDateTime lastPlayed,
            @JsonProperty("spoilersEnabled") boolean spoilersEnabled,
            @JsonProperty("starBits") int starBits,
            @JsonProperty("playTimeMinutes") long playTimeMinutes,
            @JsonProperty("characterMode") CharacterMode characterMode) {
        this.playerId = playerId;
        this.displayName = displayName;
        this.gameProgress = gameProgress != null ? new HashMap<>(gameProgress) : new HashMap<>();
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.lastPlayed = lastPlayed != null ? lastPlayed : LocalDateTime.now();
        this.spoilersEnabled = spoilersEnabled;
        this.starBits = starBits;
        this.playTimeMinutes = playTimeMinutes;
        this.characterMode = characterMode != null ? characterMode : CharacterMode.MARIO;
    }

    // create with just the basics
    public PlayerProfile(String playerId, String displayName) {
        this(playerId, displayName, null, null, null, false, 0, 0, CharacterMode.MARIO);
    }

    // create with a random id
    public PlayerProfile(String displayName) {
        this(UUID.randomUUID().toString(), displayName);
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isSpoilersEnabled() {
        return spoilersEnabled;
    }

    public void setSpoilersEnabled(boolean spoilersEnabled) {
        this.spoilersEnabled = spoilersEnabled;
    }

    // get or create game progress (lazy)
    public GameProgress getOrCreateGameProgress(String gameId) {
        return gameProgress.computeIfAbsent(gameId, GameProgress::new);
    }

    // get game progress if it exists
    public GameProgress getGameProgress(String gameId) {
        return gameProgress.get(gameId);
    }

    // games where you've actually collected something
    public List<GameProgress> getActiveGames() {
        return gameProgress.values().stream()
                .filter(gp -> gp.getCollectedCount() > 0)
                .collect(Collectors.toList());
    }

    // stars across all games
    public int getTotalStarsCollected() {
        return gameProgress.values().stream()
                .mapToInt(GameProgress::getCollectedCount)
                .sum();
    }

    // all progress for saving
    public Map<String, GameProgress> getAllGameProgress() {
        return new HashMap<>(gameProgress);
    }

    public LocalDateTime getLastPlayed() {
        return lastPlayed;
    }

    // update last played timestamp
    public void updateLastPlayed() {
        this.lastPlayed = LocalDateTime.now();
    }

    public int getStarBits() {
        return starBits;
    }

    public void setStarBits(int starBits) {
        this.starBits = starBits;
    }

    // add star bits
    public void addStarBits(int amount) {
        this.starBits += amount;
    }

    public long getPlayTimeMinutes() {
        return playTimeMinutes;
    }

    // format play time nicely like "2h 30m"
    public String getFormattedPlayTime() {
        long hours = playTimeMinutes / 60;
        long minutes = playTimeMinutes % 60;
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        }
        return String.format("%dm", minutes);
    }

    // add to play time
    public void addPlayTime(long minutes) {
        this.playTimeMinutes += minutes;
    }

    public CharacterMode getCharacterMode() {
        return characterMode;
    }

    public void setCharacterMode(CharacterMode characterMode) {
        this.characterMode = characterMode;
    }

    // is luigi unlocked? (need 120 mario stars)
    public boolean isLuigiModeUnlocked() {
        GameProgress smgProgress = gameProgress.get("super-mario-galaxy");
        if (smgProgress == null) {
            return false;
        }
        // count mario stars only
        return getMarioStarCount(smgProgress) >= CharacterMode.LUIGI.getUnlockRequirement();
    }

    // mario stars (excludes green stars)
    public int getMarioStarCount(GameProgress progress) {
        return (int) progress.getAllStarProgress().values().stream()
                .filter(sp -> !sp.getStarId().startsWith("luigi-") 
                           && !sp.getStarId().contains("green-star")
                           && sp.isCollected())
                .count();
    }

    // luigi stars (excludes green stars)
    public int getLuigiStarCount(GameProgress progress) {
        return (int) progress.getAllStarProgress().values().stream()
                .filter(sp -> sp.getStarId().startsWith("luigi-") 
                           && !sp.getStarId().contains("green-star")
                           && sp.isCollected())
                .count();
    }

    @Override
    public String toString() {
        return String.format("PlayerProfile[%s] - %d total stars across %d games",
                displayName, getTotalStarsCollected(), getActiveGames().size());
    }
}
