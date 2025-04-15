package com.stealnshare.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.stealnshare.common.GameConfig;

public class GameServer {
    private ServerSocket serverSocket;
    private PlayerHandler[] players;
    private int numRounds; // Number of rounds set to default
    private int currentRound;
    
    public GameServer() {
        players = new PlayerHandler[2];
        numRounds = GameConfig.DEFAULT_ROUNDS; // Set default rounds
        try {
            serverSocket = new ServerSocket(GameConfig.PORT);
            System.out.println("Server started on port " + GameConfig.PORT);
        } catch (IOException e) {
            System.err.println("Could not listen on port " + GameConfig.PORT);
            System.exit(1);
        }
    }
    
    public void start() {
        try {
            // Wait for two players to connect
            System.out.println("Waiting for players to connect...");
            for (int i = 0; i < 2; i++) {
                Socket clientSocket = serverSocket.accept();
                players[i] = new PlayerHandler(clientSocket, i);
                System.out.println("Player " + (i + 1) + " connected");
            }
            
            // Initialize player coins to 0
            players[0].coins = 0;
            players[1].coins = 0;
            
            // Start the game
            players[0].out.println(GameConfig.GAME_START);
            players[1].out.println(GameConfig.GAME_START);
            
            // Play rounds
            for (currentRound = 1; currentRound <= numRounds; currentRound++) {
                boolean playerDisconnected = playRound();
                if (playerDisconnected) {
                    System.out.println("[SERVER] Player disconnected. Ending game after round " + currentRound);
                    break; // End the game early if a player disconnected
                }
            }
            
            // Game over
            players[0].out.println(GameConfig.GAME_OVER);
            players[1].out.println(GameConfig.GAME_OVER);
            
        } catch (IOException e) {
            System.err.println("Error in game: " + e.getMessage());
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server: " + e.getMessage());
            }
        }
    }
    
    private boolean playRound() throws IOException {
        // Announce round
        String roundMsg = String.format(GameConfig.ROUND_FORMAT, currentRound);
        System.out.println("[SERVER] Announcing round " + currentRound + " to both players");
        players[0].out.println(roundMsg);
        players[1].out.println(roundMsg);
        System.out.println("[SERVER] Round announcement sent to both players");
        
        // Get moves with timeout
        System.out.println("[SERVER] Waiting for moves from both players for round " + currentRound);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<String> move1Future = executor.submit(() -> {
            try {
                System.out.println("[SERVER] Waiting for Player 1's move...");
                String move = players[0].in.readLine();
                System.out.println("[SERVER] Received move from Player 1: " + move);
                return move;
            } catch (IOException e) {
                System.err.println("[SERVER] Error reading from player 1: " + e.getMessage());
                return null;
            }
        });
        
        Future<String> move2Future = executor.submit(() -> {
            try {
                System.out.println("[SERVER] Waiting for Player 2's move...");
                String move = players[1].in.readLine();
                System.out.println("[SERVER] Received move from Player 2: " + move);
                return move;
            } catch (IOException e) {
                System.err.println("[SERVER] Error reading from player 2: " + e.getMessage());
                return null;
            }
        });
        
        String move1 = GameConfig.SHARE; // Default to SHARE
        String move2 = GameConfig.SHARE; // Default to SHARE
        boolean player1Timeout = false;
        boolean player2Timeout = false;
        
        try {
            // Try to get player 1's move with timeout
            try {
                System.out.println("[SERVER] Waiting up to " + GameConfig.MOVE_TIMEOUT_SECONDS + " seconds for Player 1's move");
                String tempMove1 = move1Future.get(GameConfig.MOVE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (tempMove1 != null) {
                    move1 = tempMove1;
                    System.out.println("[SERVER] Player 1 chose: " + move1);
                } else {
                    player1Timeout = true;
                    System.out.println("[SERVER] Player 1 move was null - defaulting to SHARE");
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                player1Timeout = true;
                System.out.println("[SERVER] Player 1 timed out or error: " + e.getMessage());
            }
            
            // Try to get player 2's move with timeout
            try {
                System.out.println("[SERVER] Waiting up to " + GameConfig.MOVE_TIMEOUT_SECONDS + " seconds for Player 2's move");
                String tempMove2 = move2Future.get(GameConfig.MOVE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (tempMove2 != null) {
                    move2 = tempMove2;
                    System.out.println("[SERVER] Player 2 chose: " + move2);
                } else {
                    player2Timeout = true;
                    System.out.println("[SERVER] Player 2 move was null - defaulting to SHARE");
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                player2Timeout = true;
                System.out.println("[SERVER] Player 2 timed out or error: " + e.getMessage());
            }
            
        } finally {
            executor.shutdownNow();
            System.out.println("[SERVER] Executor shutdown after getting player moves");
        }
        
        // Validate move values to ensure they are always valid
        if (!move1.equals(GameConfig.STEAL) && !move1.equals(GameConfig.SHARE)) {
            move1 = GameConfig.SHARE; // Default to SHARE for invalid values
            System.out.println("[SERVER] Player 1 move was invalid, set to SHARE");
        }
        
        if (!move2.equals(GameConfig.STEAL) && !move2.equals(GameConfig.SHARE)) {
            move2 = GameConfig.SHARE; // Default to SHARE for invalid values
            System.out.println("[SERVER] Player 2 move was invalid, set to SHARE");
        }
        
        System.out.println("[SERVER] Final moves - Player 1: " + move1 + ", Player 2: " + move2);
        if (player1Timeout) System.out.println("[SERVER] Player 1 timed out - defaulted to SHARE");
        if (player2Timeout) System.out.println("[SERVER] Player 2 timed out - defaulted to SHARE");
        
        // Process moves and update coins according to new rules
        if (move1.equals(GameConfig.SHARE) && move2.equals(GameConfig.SHARE)) {
            // Both share - both gain 3 coins
            players[0].coins += GameConfig.BOTH_SHARE_REWARD;
            players[1].coins += GameConfig.BOTH_SHARE_REWARD;
            System.out.println("[SERVER] Both players SHARE - both get 3 coins");
        } else if (move1.equals(GameConfig.STEAL) && move2.equals(GameConfig.SHARE)) {
            // Player 1 steals - gains 5 coins, player 2 gets 0
            players[0].coins += GameConfig.STEAL_FROM_SHARE_REWARD;
            players[1].coins += GameConfig.SHARE_GETS_STOLEN_REWARD;
            System.out.println("[SERVER] Player 1 STEALS, Player 2 SHARES - Player 1 gets 5 coins");
        } else if (move1.equals(GameConfig.SHARE) && move2.equals(GameConfig.STEAL)) {
            // Player 2 steals - gains 5 coins, player 1 gets 0
            players[0].coins += GameConfig.SHARE_GETS_STOLEN_REWARD;
            players[1].coins += GameConfig.STEAL_FROM_SHARE_REWARD;
            System.out.println("[SERVER] Player 1 SHARES, Player 2 STEALS - Player 2 gets 5 coins");
        } else if (move1.equals(GameConfig.STEAL) && move2.equals(GameConfig.STEAL)) {
            // Both steal - both get 1 coin
            players[0].coins += GameConfig.BOTH_STEAL_REWARD;
            players[1].coins += GameConfig.BOTH_STEAL_REWARD;
            System.out.println("[SERVER] Both players STEAL - both get 1 coin");
        }
        
        // Send player-specific result messages with timeout flags
        // Create special format that includes timeout information
        String resultMsgPlayer1 = String.format(GameConfig.RESULT_FORMAT, 
            move1, move2, players[0].coins, players[1].coins) + 
            (player1Timeout ? ":TIMEOUT" : "");
            
        String resultMsgPlayer2 = String.format(GameConfig.RESULT_FORMAT, 
            move2, move1, players[1].coins, players[0].coins) + 
            (player2Timeout ? ":TIMEOUT" : "");
        
        System.out.println("[SERVER] Sending result to Player 1: " + resultMsgPlayer1);
        players[0].out.println(resultMsgPlayer1);
        System.out.println("[SERVER] Flushing output to Player 1");
        players[0].out.flush(); // Explicitly flush the output
            
        System.out.println("[SERVER] Sending result to Player 2: " + resultMsgPlayer2);
        players[1].out.println(resultMsgPlayer2);
        System.out.println("[SERVER] Flushing output to Player 2");
        players[1].out.flush(); // Explicitly flush the output
        
        // Log the round result
        System.out.println("[SERVER] Round " + currentRound + " complete. Player 1 coins: " + 
            players[0].coins + ", Player 2 coins: " + players[1].coins);
            
        // Return true if either player disconnected
        return player1Timeout || player2Timeout;
    }
    
    private class PlayerHandler {
        Socket socket;
        BufferedReader in;
        PrintWriter out;
        int coins; // Renamed from balance to coins
        int id;
        
        public PlayerHandler(Socket socket, int id) throws IOException {
            this.socket = socket;
            this.id = id;
            this.coins = 0;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        }
    }
    
    public static void main(String[] args) {
        new GameServer().start();
    }
}
