package com.completionist.model;

import com.completionist.progress.GameProgress;

// purple coin comets are special - they need you to:
// 1. beat bowser's galaxy reactor
// 2. complete gateway's purple coins
// stricter than normal prankster comets
public class PurpleCometUnlockCondition implements UnlockCondition {
    private static final String BOWSER_GALAXY_REACTOR_STAR = "bowser-galaxy-reactor-fate";
    private static final String GATEWAY_PURPLE_COINS_STAR = "gateway-purple-coins";

    @Override
    public boolean isMet(Object context) {
        if (!(context instanceof GameProgress)) {
            return false;
        }

        GameProgress progress = (GameProgress) context;

        // gotta beat bowser first
        if (!progress.isStarCollected(BOWSER_GALAXY_REACTOR_STAR)) {
            return false;
        }

        // and the gateway purple coins
        if (!progress.isStarCollected(GATEWAY_PURPLE_COINS_STAR)) {
            return false;
        }

        return true;
    }

    @Override
    public String getDescription() {
        return "Beat Bowser's Galaxy Reactor and complete Gateway's Purple Coins";
    }
}
