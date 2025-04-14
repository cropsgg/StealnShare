package com.stealnshare.client;

import java.util.Random;

import com.stealnshare.common.GameConfig;

/**
 * Random strategy: Randomly chooses between STEAL and SHARE
 */
public class RandomStrategy implements MoveStrategy {
    private final Random random = new Random();
    
    @Override
    public String getNextMove(String opponentLastMove, int currentRound) {
        // 50% chance for each move
        return random.nextBoolean() ? GameConfig.STEAL : GameConfig.SHARE;
    }
    
    @Override
    public String getName() {
        return "Random";
    }
} 