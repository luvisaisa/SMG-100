package com.completionist.model;

import com.completionist.progress.GameProgress;
import java.util.List;

// planet of trials unlock condition
// need all 3 green stars OR both mario and luigi with 120 stars
// green stars are hidden in battlerock, dusty dune, and buoy base
public class GreenStarsUnlockCondition implements UnlockCondition {
    private static final List<String> GREEN_STAR_IDS = List.of(
        "battlerock-green-star",
        "dusty-dune-green-star",
        "buoy-base-green-star"
    );
    private static final int REQUIRED_STARS_PER_CHARACTER = 120;

    @Override
    public boolean isMet(Object context) {
        if (!(context instanceof GameProgress)) {
            return false;
        }

        GameProgress progress = (GameProgress) context;
        
        // Check if both Mario and Luigi have 120 stars (alternative unlock)
        if (areBothCharactersAt120Stars(progress)) {
            return true;
        }
        
        // all 3 green stars needed (using current mode)
        for (String greenStarId : GREEN_STAR_IDS) {
            if (!progress.isStarCollected(greenStarId)) {
                return false;
            }
        }
        return true;
    }
    
    // check if mario and luigi both have 120 (alternate unlock path)
    private boolean areBothCharactersAt120Stars(GameProgress progress) {
        // count mario stars (no prefix, skip grand finale)
        int marioStars = (int) progress.getAllStarProgress().values().stream()
                .filter(sp -> !sp.getStarId().startsWith("luigi-") && 
                              !sp.getStarId().equals("grand-finale-star-festival") &&
                              sp.isCollected())
                .count();
        
        // count luigi stars (luigi- prefix, skip grand finale)
        int luigiStars = (int) progress.getAllStarProgress().values().stream()
                .filter(sp -> sp.getStarId().startsWith("luigi-") && 
                              !sp.getStarId().equals("luigi-grand-finale-star-festival") &&
                              sp.isCollected())
                .count();

        return marioStars >= REQUIRED_STARS_PER_CHARACTER && 
               luigiStars >= REQUIRED_STARS_PER_CHARACTER;
    }

    // check for a specific character mode
    public boolean isMetForMode(GameProgress progress, CharacterMode mode) {
        // alternate path works regardless of mode
        if (areBothCharactersAt120Stars(progress)) {
            return true;
        }
        
        String prefix = mode.getStarPrefix();
        
        for (String greenStarId : GREEN_STAR_IDS) {
            if (!progress.isStarCollected(prefix + greenStarId)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getDescription() {
        return "Collect all 3 Green Power Stars, or complete game with both Mario and Luigi (120 stars each)";
    }

    // the list of green star ids
    public List<String> getGreenStarIds() {
        return GREEN_STAR_IDS;
    }

    // how many green stars collected for a mode
    public int getCollectedCount(GameProgress progress, CharacterMode mode) {
        String prefix = mode.getStarPrefix();
        int count = 0;
        for (String greenStarId : GREEN_STAR_IDS) {
            if (progress.isStarCollected(prefix + greenStarId)) {
                count++;
            }
        }
        return count;
    }
}
