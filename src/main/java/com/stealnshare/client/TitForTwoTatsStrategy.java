package com.stealnshare.client;

import com.stealnshare.common.GameConfig;

public class TitForTwoTatsStrategy implements MoveStrategy {
    private int consecutiveDefects = 0;
    
    @Override
    public String getNextMove(String opponentLastMove, int currentRound) {
        if (opponentLastMove != null) {
            if (opponentLastMove.equals(GameConfig.STEAL)) {
                consecutiveDefects++;
            } else {
                consecutiveDefects = 0;
            }
            
            if (consecutiveDefects >= 2) {
                return GameConfig.STEAL;
            }
        }
        
        return GameConfig.SHARE;
    }
    
    @Override
    public String getName() {
        return "Tit for Two Tats";
    }
} 