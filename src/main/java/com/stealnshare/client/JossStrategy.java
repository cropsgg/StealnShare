package com.stealnshare.client;

import java.util.Random;

import com.stealnshare.common.GameConfig;

public class JossStrategy implements MoveStrategy {
    private Random random = new Random();
    
    @Override
    public String getNextMove(String opponentLastMove, int currentRound) {
        // 10% chance to defect regardless of opponent's move
        if (random.nextDouble() < 0.1) {
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
        return "Joss";
    }
} 