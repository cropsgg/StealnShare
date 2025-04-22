package com.stealnshare.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.stealnshare.common.GameConfig;

public class RakshithaStrategy implements MoveStrategy {
    private Random random = new Random();
    private List<String> lastMoves = new ArrayList<>();
    private List<Integer> moveRewards = new ArrayList<>();
    
    @Override
    public String getNextMove(String opponentLastMove, int currentRound) {
        // First 10 moves are random
        if (currentRound <= 10) {
            String move = random.nextBoolean() ? GameConfig.STEAL : GameConfig.SHARE;
            lastMoves.add(move);
            moveRewards.add(0); // Initialize with 0 reward
            return move;
        }
        
        // Calculate success rate of last 10 moves
        int stealCount = 0;
        int shareCount = 0;
        int stealReward = 0;
        int shareReward = 0;
        
        // Only process if we have enough moves
        if (lastMoves.size() >= 10) {
            for (int i = lastMoves.size() - 1; i >= Math.max(0, lastMoves.size() - 10); i--) {
                String move = lastMoves.get(i);
                int reward = moveRewards.get(i);
                
                if (move.equals(GameConfig.STEAL)) {
                    stealCount++;
                    stealReward += reward;
                } else {
                    shareCount++;
                    shareReward += reward;
                }
            }
        }
        
        // Calculate average reward per move
        double stealAvg = stealCount > 0 ? (double) stealReward / stealCount : 0;
        double shareAvg = shareCount > 0 ? (double) shareReward / shareCount : 0;
        
        // Choose move with higher average reward
        String move = stealAvg >= shareAvg ? GameConfig.STEAL : GameConfig.SHARE;
        lastMoves.add(move);
        moveRewards.add(0); // Initialize with 0 reward
        return move;
    }
    
    public void updateReward(int reward) {
        if (!moveRewards.isEmpty()) {
            moveRewards.set(moveRewards.size() - 1, reward);
        }
    }
    
    @Override
    public String getName() {
        return "Rakshitha";
    }
} 