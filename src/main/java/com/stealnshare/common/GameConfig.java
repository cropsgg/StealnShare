package com.stealnshare.common;

public class GameConfig {
    public static final int PORT = 8080;
    public static final String STEAL = "STEAL";
    public static final String SHARE = "SHARE";
    public static final String GAME_START = "GAME_START";
    public static final String ROUND_FORMAT = "ROUND:%d";
    public static final String GAME_OVER = "GAME_OVER";
    public static final int MOVE_TIMEOUT_SECONDS = 30;
    public static final String RESULT_FORMAT = "RESULT:%s:%s:%d:%d"; // format: RESULT:player1Move:player2Move:player1Coins:player2Coins
    public static final int DEFAULT_ROUNDS = 15; // Default number of rounds
    
    // New reward constants based on updated rules
    public static final int BOTH_STEAL_REWARD = 1; // Each gets 1 coin
    public static final int BOTH_SHARE_REWARD = 3; // Each gets 3 coins
    public static final int STEAL_FROM_SHARE_REWARD = 5; // Stealer gets 5 coins
    public static final int SHARE_GETS_STOLEN_REWARD = 0; // Sharer gets 0 coins
} 