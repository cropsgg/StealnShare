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
    private int totalMoney;
    private int numRounds;
    private int currentRound;
    
    public GameServer() {
        players = new PlayerHandler[2];
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
            
            // Get game configuration from first player
            String config = players[0].in.readLine();
            if (config.startsWith("CONFIG:")) {
                String[] parts = config.split(":");
                totalMoney = Integer.parseInt(parts[1]);
                numRounds = Integer.parseInt(parts[2]);
                players[0].balance = totalMoney;
                players[1].balance = totalMoney;
            }
            
            // Start the game
            players[0].out.println(GameConfig.GAME_START);
            players[1].out.println(GameConfig.GAME_START);
            
            // Play rounds
            for (currentRound = 1; currentRound <= numRounds; currentRound++) {
                playRound();
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
    
    private void playRound() throws IOException {
        // Announce round
        String roundMsg = String.format(GameConfig.ROUND_FORMAT, currentRound);
        players[0].out.println(roundMsg);
        players[1].out.println(roundMsg);
        
        // Get moves with timeout
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<String> move1Future = executor.submit(() -> players[0].in.readLine());
        Future<String> move2Future = executor.submit(() -> players[1].in.readLine());
        
        String move1 = null, move2 = null;
        try {
            move1 = move1Future.get(GameConfig.MOVE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            move2 = move2Future.get(GameConfig.MOVE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // If timeout or error, default to STEAL
            if (move1 == null) move1 = GameConfig.STEAL;
            if (move2 == null) move2 = GameConfig.STEAL;
        }
        executor.shutdownNow();
        
        // Process moves and update balances
        if (move1.equals(GameConfig.SHARE) && move2.equals(GameConfig.SHARE)) {
            // Both share - both gain 100
            players[0].balance += 100;
            players[1].balance += 100;
        } else if (move1.equals(GameConfig.STEAL) && move2.equals(GameConfig.SHARE)) {
            // Player 1 steals - gains 200
            players[0].balance += 200;
        } else if (move1.equals(GameConfig.SHARE) && move2.equals(GameConfig.STEAL)) {
            // Player 2 steals - gains 200
            players[1].balance += 200;
        }
        // Both steal - no changes
        
        // Send results
        String resultMsg = String.format(GameConfig.RESULT_FORMAT, move1, move2, 
            players[0].balance, players[1].balance);
        players[0].out.println(resultMsg);
        players[1].out.println(resultMsg);
    }
    
    private class PlayerHandler {
        Socket socket;
        BufferedReader in;
        PrintWriter out;
        int balance;
        int id;
        
        public PlayerHandler(Socket socket, int id) throws IOException {
            this.socket = socket;
            this.id = id;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        }
    }
    
    public static void main(String[] args) {
        new GameServer().start();
    }
}
