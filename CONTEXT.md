# Steal and Share: LAN-based Gaming Application

## Overview
This project implements a LAN-based gaming application using a **2 Client – 1 Server** architecture in Java. The game is called **"Steal and Share"** where two players bet an amount and play a fixed number of rounds. In each round, the bet is computed as:


The game rules are as follows:
- **Both choose STEAL:** Both players lose the bet amount.
- **Both choose SHARE:** Both players keep their money (no change in balance).
- **One chooses STEAL and the other chooses SHARE:** The player who chooses **STEAL** wins the bet amount from the opponent (i.e. gains the opponent’s bet, while the sharer loses that amount).

Additional game features:
1. The best design decisions have been applied to ensure a clean, efficient implementation.
2. At the end of the game, each player is informed of their final earnings.
3. In every round, the server waits for both players’ moves. It allows up to **5 seconds** per player. If no input is received within that window, the move defaults to **SHARE**.
4. The application runs a **single game session**.
5. The only move options are **STEAL** and **SHARE**.

The client’s UI is designed using Java Swing with a retro, 1990s style. The game name "Steal and Share" is displayed prominently at the start, and a footer in the bottom right displays **"Cn lab project"**.

## System Architecture

### Components
- **Server**:  
  - Listens for connections from two clients.
  - Receives game configuration (total money and number of rounds) from both clients (assumed to be identical).
  - Manages the game rounds:
    - Broadcasts round information (round number and bet amount) to both clients.
    - Waits up to 5 seconds for each client’s move (defaults to SHARE if no input is received).
    - Processes the moves based on the game rules and updates player balances.
  - Sends round results and, at the end, final earnings to both clients.

- **Client**:  
  - Presents a basic retro-style GUI using Java Swing.
  - Prompts the user for initial configuration (total money and number of rounds).
  - Displays the game name ("Steal and Share") and a footer ("Cn lab project").
  - Provides two buttons (STEAL and SHARE) for the user to make their move each round.
  - Communicates with the server to send moves and receive game updates.

## Game Flow

1. **Initial Setup:**
   - Both clients enter the total money and number of rounds via input dialogs.
   - Each client sends a configuration message to the server in the format:
     ```
     CONFIG:<totalMoney>:<numRounds>
     ```
   - The server initializes each player’s balance to the provided total money and calculates the bet amount per round.

2. **Game Rounds:**
   - The server sends a message to both clients at the start of each round:
     ```
     ROUND:<roundNumber>:BET:<betAmount>
     ```
   - For each round, the server waits up to 5 seconds for each client's move. If a player does not respond in time, their move is defaulted to **SHARE**.
   - The server processes the moves according to the rules:
     - **Both STEAL:** Both players lose the bet amount.
     - **Both SHARE:** No change in either balance.
     - **One STEAL, One SHARE:** The stealer gains the bet amount while the sharer loses it.
   - Round results (including updated balances) are sent to both clients.

3. **Game Conclusion:**
   - After all rounds, the server sends a final message to each client indicating their final balance (and net earnings).
   - Clients display these final results in their UI.

## Implementation Details

### Server Code (Java)

```java
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class GameServer {
    private static final int PORT = 12345;
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
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started on port " + PORT);
        
        // Wait for two clients to connect
        System.out.println("Waiting for Client1...");
        client1 = serverSocket.accept();
        System.out.println("Client1 connected: " + client1.getInetAddress());
        
        System.out.println("Waiting for Client2...");
        client2 = serverSocket.accept();
        System.out.println("Client2 connected: " + client2.getInetAddress());
        
        in1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));
        out1 = new PrintWriter(client1.getOutputStream(), true);
        
        in2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));
        out2 = new PrintWriter(client2.getOutputStream(), true);
        
        // Receive initial configuration from both clients
        // Expected format: CONFIG:<totalMoney>:<numRounds>
        try {
            String config1 = in1.readLine();
            String config2 = in2.readLine();
            System.out.println("Received configs: " + config1 + " , " + config2);
            
            // For simplicity, assume both clients send identical configurations and use Client1's values.
            String[] parts = config1.split(":");
            totalMoney = Integer.parseInt(parts[1]);
            numRounds = Integer.parseInt(parts[2]);
            
            // Initialize player balances and calculate bet amount
            balance1 = totalMoney;
            balance2 = totalMoney;
            betAmount = totalMoney / numRounds;
            
            // Inform both clients that the game is starting with the game name
            out1.println("GAME_START: Steal and Share");
            out2.println("GAME_START: Steal and Share");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        // Start game rounds
        playGame();
        
        // Close connections after the game
        try {
            client1.close();
            client2.close();
            System.out.println("Game over. Server closed connections.");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    // Main game loop handling rounds sequentially
    private void playGame() {
        while (currentRound <= numRounds) {
            // Inform both clients about the new round and the bet amount
            out1.println("ROUND:" + currentRound + ":BET:" + betAmount);
            out2.println("ROUND:" + currentRound + ":BET:" + betAmount);
            
            // Collect moves from both clients with a 5-second timeout per player
            String move1 = getMoveWithTimeout(in1);
            String move2 = getMoveWithTimeout(in2);
            System.out.println("Round " + currentRound + " moves: Client1=" + move1 + ", Client2=" + move2);
            
            // Process round results based on game rules
            processRound(move1, move2);
            currentRound++;
        }
        
        // Send final results to both clients
        out1.println("GAME_OVER: Your final balance is $" + balance1);
        out2.println("GAME_OVER: Your final balance is $" + balance2);
    }
    
    // Helper method to read a player's move with a 5-second timeout.
    private String getMoveWithTimeout(BufferedReader in) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<String> task = () -> in.readLine();
        Future<String> future = executor.submit(task);
        String move = null;
        try {
            // Wait up to 5 seconds for the move
            move = future.get(5, TimeUnit.SECONDS);
            if(move == null || move.trim().isEmpty()) {
                move = "SHARE";
            }
        } catch (Exception e) {
            // On timeout or error, default the move to SHARE
            move = "SHARE";
        }
        executor.shutdownNow();
        return move.toUpperCase().trim();
    }
    
    // Process the moves for the current round and update balances accordingly
    private void processRound(String move1, String move2) {
        if(move1.equals("STEAL") && move2.equals("STEAL")) {
            // Both lose bet amount
            balance1 -= betAmount;
            balance2 -= betAmount;
            sendRoundResult("Both chose STEAL. Both lose $" + betAmount);
        } else if(move1.equals("SHARE") && move2.equals("SHARE")) {
            // No change in balance
            sendRoundResult("Both chose SHARE. No changes in balance.");
        } else if(move1.equals("STEAL") && move2.equals("SHARE")) {
            // Client1 gains bet amount; Client2 loses bet amount
            balance1 += betAmount;
            balance2 -= betAmount;
            sendRoundResult("Client1 STEALS and wins $" + betAmount + " from Client2");
        } else if(move1.equals("SHARE") && move2.equals("STEAL")) {
            // Client2 gains bet amount; Client1 loses bet amount
            balance1 -= betAmount;
            balance2 += betAmount;
            sendRoundResult("Client2 STEALS and wins $" + betAmount + " from Client1");
        }
    }
    
    // Send round results (including updated balance) to both clients
    private void sendRoundResult(String message) {
        out1.println("RESULT:" + message + ":Balance: $" + balance1);
        out2.println("RESULT:" + message + ":Balance: $" + balance2);
    }
    
    public static void main(String[] args) {
        try {
            new GameServer();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class GameClient extends JFrame {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private JTextArea gameLog;
    private JButton stealButton, shareButton;
    private JLabel titleLabel, footerLabel;
    
    // Game parameters
    private int totalMoney;
    private int numRounds;
    
    public GameClient(String serverAddress, int port) {
        // Prompt for game configuration
        try {
            totalMoney = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter total money:"));
            numRounds = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter number of rounds:"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input. Exiting.");
            System.exit(0);
        }
        
        // Set up the retro-style UI
        setTitle("Steal and Share - 1990s Retro Edition");
        setSize(500, 400);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Title Panel (Game Name)
        titleLabel = new JLabel("Steal and Share", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);
        
        // Game log in the center
        gameLog = new JTextArea();
        gameLog.setEditable(false);
        gameLog.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(gameLog);
        add(scrollPane, BorderLayout.CENTER);
        
        // Button Panel for STEAL and SHARE options
        JPanel buttonPanel = new JPanel();
        stealButton = new JButton("STEAL");
        shareButton = new JButton("SHARE");
        buttonPanel.add(stealButton);
        buttonPanel.add(shareButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Footer label at bottom right
        footerLabel = new JLabel("Cn lab project");
        footerLabel.setFont(new Font("Monospaced", Font.ITALIC, 12));
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.add(footerLabel, BorderLayout.SOUTHEAST);
        add(footerPanel, BorderLayout.EAST);
        
        // Connect to the server
        try {
            socket = new Socket(serverAddress, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            gameLog.append("Connected to server.\n");
            
            // Send configuration to the server in the expected format:
            // CONFIG:<totalMoney>:<numRounds>
            out.println("CONFIG:" + totalMoney + ":" + numRounds);
            
            // Start a background thread to listen for server messages
            new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        SwingUtilities.invokeLater(() -> {
                            gameLog.append(line + "\n");
                        });
                    }
                } catch(IOException e) {
                    gameLog.append("Connection lost.\n");
                }
            }).start();
            
        } catch(IOException e) {
            JOptionPane.showMessageDialog(this, "Unable to connect to server.");
            System.exit(0);
        }
        
        // Button actions: send the move to the server when clicked
        stealButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMove("STEAL");
            }
        });
        
        shareButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMove("SHARE");
            }
        });
    }
    
    // Send the selected move to the server
    private void sendMove(String move) {
        gameLog.append("You chose: " + move + "\n");
        out.println(move);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameClient client = new GameClient("localhost", 12345);
            client.setVisible(true);
        });
    }
}
