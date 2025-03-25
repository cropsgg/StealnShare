package com.stealnshare.common;

public class GameConfig {
    public static final int PORT = 8080;
    public static final String STEAL = "STEAL";
    public static final String SHARE = "SHARE";
    public static final String CONFIG_FORMAT = "CONFIG:%d:%d"; // format: CONFIG:totalMoney:numRounds
    public static final String GAME_START = "GAME_START";
    public static final String ROUND_FORMAT = "ROUND:%d";
    public static final String GAME_OVER = "GAME_OVER";
    public static final int MOVE_TIMEOUT_SECONDS = 30;
    public static final String RESULT_FORMAT = "RESULT:%s:%s:%d:%d"; // format: RESULT:player1Move:player2Move:player1Balance:player2Balance
} 