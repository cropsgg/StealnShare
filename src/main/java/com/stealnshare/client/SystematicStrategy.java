package com.stealnshare.client;

import com.stealnshare.common.GameConfig;

/**
 * Systematic strategy: Starts with SHARE, then alternates between STEAL and SHARE
 */
public class SystematicStrategy implements MoveStrategy {
    
    @Override
    public String getNextMove(String opponentLastMove, int currentRound) {
        // First round: SHARE
        if (currentRound == 1) {
            return GameConfig.SHARE;
        }
        
        // Even rounds: STEAL, Odd rounds after first: SHARE
        return (currentRound % 2 == 0) ? GameConfig.STEAL : GameConfig.SHARE;
    }
    
    @Override
    public String getName() {
        return "Systematic";
    }
} 