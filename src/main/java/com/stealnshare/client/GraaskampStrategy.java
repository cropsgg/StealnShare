package com.stealnshare.client;

import com.stealnshare.common.GameConfig;

public class GraaskampStrategy implements MoveStrategy {
    @Override
    public String getNextMove(String opponentLastMove, int currentRound) {
        // Defect on round 50 and after
        if (currentRound >= 50) {
            return GameConfig.STEAL;
        }
        
        // Otherwise, copy opponent's last move
        if (opponentLastMove != null) {
            return opponentLastMove;
        }
        
        // First move is always SHARE
        return GameConfig.SHARE;
    }
    
    @Override
    public String getName() {
        return "Graaskamp";
    }
} 