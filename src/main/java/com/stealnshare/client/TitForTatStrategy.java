package com.stealnshare.client;

import com.stealnshare.common.GameConfig;

/**
 * Tit for Tat strategy: Always starts with SHARE, then copies opponent's previous move
 */
public class TitForTatStrategy implements MoveStrategy {
    
    @Override
    public String getNextMove(String opponentLastMove, int currentRound) {
        // First round or if we don't know opponent's last move, play SHARE
        if (opponentLastMove == null || currentRound == 1) {
            return GameConfig.SHARE;
        }
        
        // Otherwise, copy opponent's last move
        return opponentLastMove;
    }
    
    @Override
    public String getName() {
        return "Tit for Tat";
    }
} 