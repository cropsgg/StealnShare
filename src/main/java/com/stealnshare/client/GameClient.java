package com.stealnshare.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.stealnshare.common.GameConfig;

public class GameClient extends JFrame {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private JTextArea gameLog;
    private JButton stealButton, shareButton;
    private JLabel titleLabel, footerLabel, coinsLabel, roundLabel, opponentCoinsLabel;
    private JPanel mainPanel, statsPanel;
    private int currentRound = 0;
    private int totalRounds = GameConfig.DEFAULT_ROUNDS;
    private int myCoins = 0;
    private int opponentCoins = 0;
    
    // Sound clips
    private Clip stealSound;
    private Clip shareSound;
    private Clip loseSound;
    private Clip bothStealSound;
    
    // Add a new instance variable to track if the client has sent a move
    private boolean moveSentForCurrentRound = false;
    
    public GameClient() {
        setupUI();
        loadSounds();
        connectToServer();
    }
    
    private void setupUI() {
        // Basic window setup
        setTitle("Steal and Share - 1990s Retro Edition");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Main panel with border layout
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(mainPanel);
        
        // Title panel with retro styling
        JPanel titlePanel = new JPanel(new BorderLayout());
        titleLabel = new JLabel("Steal and Share", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Courier", Font.BOLD, 32));
        titleLabel.setForeground(new Color(0, 100, 0));
        titleLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Stats panel
        statsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Game Statistics"));
        
        // Round information
        roundLabel = new JLabel(String.format("Round: 0/%d", totalRounds), SwingConstants.CENTER);
        roundLabel.setFont(new Font("Courier", Font.BOLD, 16));
        statsPanel.add(roundLabel);
        
        // Coins display
        coinsLabel = new JLabel("Your Coins: 0", SwingConstants.CENTER);
        coinsLabel.setFont(new Font("Courier", Font.BOLD, 16));
        statsPanel.add(coinsLabel);
        
        // Opponent coins
        opponentCoinsLabel = new JLabel("Opponent Coins: 0", SwingConstants.CENTER);
        opponentCoinsLabel.setFont(new Font("Courier", Font.BOLD, 16));
        statsPanel.add(opponentCoinsLabel);
        
        mainPanel.add(statsPanel, BorderLayout.NORTH);
        
        // Game log in the center with retro styling
        gameLog = new JTextArea();
        gameLog.setEditable(false);
        gameLog.setFont(new Font("Courier", Font.PLAIN, 14));
        gameLog.setBackground(new Color(240, 240, 240));
        gameLog.setForeground(new Color(0, 100, 0));
        gameLog.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        JScrollPane scrollPane = new JScrollPane(gameLog);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Control panel for buttons
        JPanel controlPanel = new JPanel(new BorderLayout(10, 10));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        stealButton = createRetroButton("STEAL", new Color(200, 0, 0));
        shareButton = createRetroButton("SHARE", new Color(0, 100, 0));
        buttonPanel.add(stealButton);
        buttonPanel.add(shareButton);
        controlPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // Footer
        footerLabel = new JLabel("© 2025 Computer Network project  Steal and Share", SwingConstants.RIGHT);
        footerLabel.setFont(new Font("Courier", Font.ITALIC, 12));
        controlPanel.add(footerLabel, BorderLayout.SOUTH);
        
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        // Button actions
        stealButton.addActionListener(e -> sendMove(GameConfig.STEAL));
        shareButton.addActionListener(e -> sendMove(GameConfig.SHARE));
        
        // Disable buttons initially
        stealButton.setEnabled(false);
        shareButton.setEnabled(false);
    }
    
    private JButton createRetroButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Courier", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        return button;
    }
    
    private void connectToServer() {
        try {
            // Connect to server with default settings (no more prompts)
            socket = new Socket("localhost", GameConfig.PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // Set default values
            myCoins = 0;
            opponentCoins = 0;
            
            // Update labels
            coinsLabel.setText("Your Coins: 0");
            opponentCoinsLabel.setText("Opponent Coins: 0");
            roundLabel.setText(String.format("Round: 0/%d", totalRounds));
            
            // Start message listener thread
            new Thread(this::listenForMessages).start();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error connecting to server: " + e.getMessage(),
                "Connection Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    private void listenForMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                final String msg = message;
                SwingUtilities.invokeLater(() -> processMessage(msg));
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                gameLog.append("Connection lost.\n");
                stealButton.setEnabled(false);
                shareButton.setEnabled(false);
            });
        }
    }
    
    private void loadSounds() {
        try {
            // Load sound files from resources
            stealSound = loadSound("/sounds/steal.wav");
            shareSound = loadSound("/sounds/share.wav");
            loseSound = loadSound("/sounds/lose.wav");
            bothStealSound = loadSound("/sounds/both_steal.wav");
        } catch (Exception e) {
            System.err.println("Error loading sounds: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Warning: Could not load sound effects. Game will continue without sound.",
                "Sound Loading Error",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private Clip loadSound(String resourcePath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
            getClass().getResourceAsStream(resourcePath));
        Clip clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        return clip;
    }
    
    private void playSound(Clip clip) {
        if (clip != null) {
            try {
                clip.setFramePosition(0);
                clip.start();
            } catch (Exception e) {
                System.err.println("Error playing sound: " + e.getMessage());
            }
        }
    }
    
    private void processMessage(String message) {
        if (message.startsWith("ROUND:")) {
            currentRound = Integer.parseInt(message.split(":")[1]);
            roundLabel.setText(String.format("Round: %d/%d", currentRound, totalRounds));
            stealButton.setEnabled(true);
            shareButton.setEnabled(true);
            moveSentForCurrentRound = false; // Reset the flag for the new round
            
            // Add a message to inform the player
            gameLog.append(message + "\n");
            gameLog.append("Please choose STEAL or SHARE for this round.\n");
        } else if (message.startsWith("RESULT:")) {
            // Parse result message
            String[] parts = message.split(":");
            String myMove = parts[1];         // First move is always this player's move
            String opponentMove = parts[2];   // Second move is always opponent's move
            int myNewCoins = Integer.parseInt(parts[3]);  // First coins value is always this player's coins
            int opponentNewCoins = Integer.parseInt(parts[4]); // Second coins value is always opponent's coins
            
            // Check if this was a timeout for this player
            boolean wasTimeout = message.endsWith(":TIMEOUT");
            
            // If we didn't send a move or there was a timeout, it means our move defaulted to SHARE
            if (!moveSentForCurrentRound || wasTimeout) {
                gameLog.append("Your move timed out - defaulted to SHARE.\n");
                myMove = GameConfig.SHARE; // Ensure move is set to SHARE in case of timeout
            }
            
            // Calculate coins gained in this round
            int coinsGained = myNewCoins - myCoins;
            int opponentCoinsGained = opponentNewCoins - opponentCoins;
            
            // Update coins
            myCoins = myNewCoins;
            opponentCoins = opponentNewCoins;
            coinsLabel.setText(String.format("Your Coins: %d", myCoins));
            opponentCoinsLabel.setText(String.format("Opponent Coins: %d", opponentCoins));
            
            // Format and display round result (now that both players have decided)
            // This only happens at the end of the round after both players have chosen
            String resultMessage = String.format("\nRound %d Result:\n", currentRound);
            resultMessage += String.format("You chose: %s\n", myMove);
            resultMessage += String.format("Opponent chose: %s\n", opponentMove);
            resultMessage += String.format("Coins gained: %d\n", coinsGained);
            resultMessage += String.format("Opponent coins gained: %d\n", opponentCoinsGained);
            gameLog.append(resultMessage);
            
            // Ensure buttons are disabled for this round
            stealButton.setEnabled(false);
            shareButton.setEnabled(false);
            
            // Play appropriate sound based on the result
            if (myMove.equals(GameConfig.STEAL) && opponentMove.equals(GameConfig.SHARE)) {
                playSound(stealSound);
            } else if (myMove.equals(GameConfig.SHARE) && opponentMove.equals(GameConfig.SHARE)) {
                playSound(shareSound);
            } else if (myMove.equals(GameConfig.STEAL) && opponentMove.equals(GameConfig.STEAL)) {
                playSound(bothStealSound);
            } else if (myMove.equals(GameConfig.SHARE) && opponentMove.equals(GameConfig.STEAL)) {
                playSound(loseSound);
            }
        } else if (message.equals(GameConfig.GAME_OVER)) {
            stealButton.setEnabled(false);
            shareButton.setEnabled(false);
            gameLog.append("\n" + message + "\n");
            gameLog.append(String.format("Final Coins: %d\n", myCoins));
            gameLog.append(String.format("Opponent's Final Coins: %d\n", opponentCoins));
        } else {
            // For other messages, just display them
            gameLog.append(message + "\n");
        }
        
        // Always scroll to the bottom when new content is added
        gameLog.setCaretPosition(gameLog.getDocument().getLength());
    }
    
    private void sendMove(String move) {
        out.println(move);
        stealButton.setEnabled(false);
        shareButton.setEnabled(false);
        moveSentForCurrentRound = true; // Mark that we've sent a move for this round
        
        // Just inform the player about their choice - don't show opponent's choice
        gameLog.append("You selected: " + move + "\n");
        gameLog.append("Waiting for round to complete...\n");
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error setting look and feel: " + e.getMessage());
        }
        SwingUtilities.invokeLater(() -> new GameClient().setVisible(true));
    }
} 