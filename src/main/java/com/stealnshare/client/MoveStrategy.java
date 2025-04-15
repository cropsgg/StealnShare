package com.stealnshare.client;

/**
 * Interface for algorithm strategies that can automatically play the game
 */
public interface MoveStrategy {
    /**
     * Gets the next move from the algorithm
     * @param opponentLastMove The opponent's move from the previous round, or null if first round
     * @param currentRound The current round number
     * @return The move (STEAL or SHARE)
     */
    String getNextMove(String opponentLastMove, int currentRound);
    
    /**
     * Gets the name of this strategy
     * @return The strategy name
     */
    String getName();
} 