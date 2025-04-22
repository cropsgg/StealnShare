package com.stealnshare.client;

import com.stealnshare.common.GameConfig;

public class TesterStrategy implements MoveStrategy {
    private boolean isNasty = false;
    private boolean firstMove = true;
    
    @Override
    public String getNextMove(String opponentLastMove, int currentRound) {
        if (firstMove) {
            firstMove = false;
            return GameConfig.STEAL;
        }
        
        if (opponentLastMove != null) {
            // If opponent retaliated with STEAL after our initial STEAL, become nasty
            if (currentRound == 2 && opponentLastMove.equals(GameConfig.STEAL)) {
                isNasty = true;
            }
        }
        
        if (isNasty) {
            return GameConfig.STEAL;
        }
        
        // Otherwise, follow Tit for Tat
        if (opponentLastMove != null) {
            return opponentLastMove;
        }
        
        return GameConfig.SHARE;
    }
    
    @Override
    public String getName() {
        return "Tester";
    }
} 