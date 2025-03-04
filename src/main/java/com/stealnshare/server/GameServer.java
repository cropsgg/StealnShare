package com.stealnshare.server;

import com.stealnshare.common.GameConfig;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class GameServer {
    private Socket client1;
    private Socket client2;
    private BufferedReader in1, in2;
    private PrintWriter out1, out2;
    
    // Game configuration and state
    private int totalMoney;
    private int numRounds;
    private int currentRound = 1;
    private int betAmount;
    private int balance1, balance2;
    
    public GameServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(GameConfig.PORT);
        System.out.println("Server started on port " + GameConfig.PORT);
        
        // Wait for two clients to connect
        System.out.println("Waiting for Client1...");
        client1 = serverSocket.accept();
        System.out.println("Client1 connected: " + client1.getInetAddress());
        
        System.out.println("Waiting for Client2...");
        client2 = serverSocket.accept();
        System.out.println("Client2 connected: " + client2.getInetAddress());
        
        setupStreams();
        initializeGame();
        playGame();
        cleanup();
    }
    
    private void setupStreams() throws IOException {
        in1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));
        out1 = new PrintWriter(client1.getOutputStream(), true);
        in2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));
        out2 = new PrintWriter(client2.getOutputStream(), true);
    }
    
    private void initializeGame() throws IOException {
        // Receive initial configuration from both clients
        String config1 = in1.readLine();
        String config2 = in2.readLine();
        System.out.println("Received configs: " + config1 + " , " + config2);
        
        // Parse configuration (using Client1's values)
        String[] parts = config1.split(":");
        totalMoney = Integer.parseInt(parts[1]);
        numRounds = Integer.parseInt(parts[2]);
        
        // Initialize game state
        balance1 = totalMoney;
        balance2 = totalMoney;
        betAmount = totalMoney / numRounds;
        
        // Inform clients game is starting
        out1.println(String.format(GameConfig.GAME_START, "Steal and Share"));
        out2.println(String.format(GameConfig.GAME_START, "Steal and Share"));
    }
    
    private void playGame() {
        while (currentRound <= numRounds) {
            // Announce round
            String roundMsg = String.format(GameConfig.ROUND_FORMAT, currentRound, betAmount);
            out1.println(roundMsg);
            out2.println(roundMsg);
            
            // Get moves
            String move1 = getMoveWithTimeout(in1);
            String move2 = getMoveWithTimeout(in2);
            System.out.println("Round " + currentRound + " moves: Client1=" + move1 + ", Client2=" + move2);
            
            processRound(move1, move2);
            currentRound++;
        }
        
        // Send final results
        out1.println(String.format(GameConfig.GAME_OVER, balance1));
        out2.println(String.format(GameConfig.GAME_OVER, balance2));
    }
    
    private String getMoveWithTimeout(BufferedReader in) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> in.readLine());
        
        try {
            String move = future.get(GameConfig.MOVE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return (move != null && !move.trim().isEmpty()) ? move.toUpperCase().trim() : GameConfig.SHARE;
        } catch (Exception e) {
            return GameConfig.SHARE;  // Default to SHARE on timeout or error
        } finally {
            executor.shutdownNow();
        }
    }
    
    private void processRound(String move1, String move2) {
        String resultMessage;
        
        if (move1.equals(GameConfig.STEAL) && move2.equals(GameConfig.STEAL)) {
            balance1 -= betAmount;
            balance2 -= betAmount;
            resultMessage = "Both chose STEAL. Both lose $" + betAmount;
        } else if (move1.equals(GameConfig.SHARE) && move2.equals(GameConfig.SHARE)) {
            resultMessage = "Both chose SHARE. No changes in balance.";
        } else if (move1.equals(GameConfig.STEAL) && move2.equals(GameConfig.SHARE)) {
            balance1 += betAmount;
            balance2 -= betAmount;
            resultMessage = "Client1 STEALS and wins $" + betAmount + " from Client2";
        } else {
            balance1 -= betAmount;
            balance2 += betAmount;
            resultMessage = "Client2 STEALS and wins $" + betAmount + " from Client1";
        }
        
        // Send round results
        out1.println(String.format(GameConfig.RESULT_FORMAT, resultMessage, balance1));
        out2.println(String.format(GameConfig.RESULT_FORMAT, resultMessage, balance2));
    }
    
    private void cleanup() {
        try {
            client1.close();
            client2.close();
            System.out.println("Game over. Server closed connections.");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        try {
            new GameServer();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
