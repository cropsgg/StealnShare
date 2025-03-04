package com.stealnshare.common;

public class GameConfig {
    public static final int PORT = 12345;
    public static final int MOVE_TIMEOUT_SECONDS = 5;
    public static final String STEAL = "STEAL";
    public static final String SHARE = "SHARE";
    
    // Message formats
    public static final String CONFIG_FORMAT = "CONFIG:%d:%d";  // totalMoney:numRounds
    public static final String ROUND_FORMAT = "ROUND:%d:BET:%d";  // roundNumber:betAmount
    public static final String GAME_START = "GAME_START: %s";
    public static final String GAME_OVER = "GAME_OVER: Your final balance is $%d";
    public static final String RESULT_FORMAT = "RESULT:%s:Balance: $%d";  // message:balance
} 