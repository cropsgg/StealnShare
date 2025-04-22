package com.stealnshare.client;

import com.stealnshare.common.GameConfig;

public class FriedmanStrategy implements MoveStrategy {
    private boolean hasOpponentDefected = false;
    
    @Override
    public String getNextMove(String opponentLastMove, int currentRound) {
        if (hasOpponentDefected) {
            return GameConfig.STEAL;
        }
        
        if (opponentLastMove != null && opponentLastMove.equals(GameConfig.STEAL)) {
            hasOpponentDefected = true;
            return GameConfig.STEAL;
        }
        
        return GameConfig.SHARE;
    }
    
    @Override
    public String getName() {
        return "Friedman";
    }
} 