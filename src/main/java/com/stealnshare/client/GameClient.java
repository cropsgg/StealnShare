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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import com.stealnshare.common.GameConfig;

public class GameClient extends JFrame {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private JTextArea gameLog;
    private JButton stealButton, shareButton, algorithmButton, readyButton;
    private JLabel titleLabel, footerLabel, coinsLabel, roundLabel, opponentCoinsLabel, algorithmLabel;
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
    
    // Algorithm variables
    private boolean usingAlgorithm = false;
    private MoveStrategy selectedStrategy = null;
    private String opponentLastMove = null;
    private Timer algorithmTimer;
    
    // Add a new instance variable to track if the client has sent a move
    private boolean moveSentForCurrentRound = false;
    
    // New instance variables for round selection and ready state
    private JComboBox<Integer> roundSelectionComboBox;
    private boolean isReady = false;
    private int selectedRounds = GameConfig.DEFAULT_ROUNDS;
    private boolean isLongGame = false;
    
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
        statsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
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
        
        // Algorithm label
        algorithmLabel = new JLabel("Algorithm: None", SwingConstants.CENTER);
        algorithmLabel.setFont(new Font("Courier", Font.BOLD, 16));
        statsPanel.add(algorithmLabel);
        
        // Round selection
        JPanel roundSelectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel roundSelectionLabel = new JLabel("Select rounds:");
        roundSelectionComboBox = new JComboBox<>(new Integer[]{15, 200});
        roundSelectionComboBox.setSelectedItem(GameConfig.DEFAULT_ROUNDS);
        roundSelectionPanel.add(roundSelectionLabel);
        roundSelectionPanel.add(roundSelectionComboBox);
        statsPanel.add(roundSelectionPanel);
        
        // Ready status
        JLabel readyStatusLabel = new JLabel("Ready: No", SwingConstants.CENTER);
        readyStatusLabel.setFont(new Font("Courier", Font.BOLD, 16));
        statsPanel.add(readyStatusLabel);
        
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
        algorithmButton = createRetroButton("PLAY WITH ALGORITHM", new Color(0, 0, 150));
        readyButton = createRetroButton("READY", new Color(150, 150, 0));
        buttonPanel.add(stealButton);
        buttonPanel.add(shareButton);
        buttonPanel.add(algorithmButton);
        buttonPanel.add(readyButton);
        controlPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // Footer
        footerLabel = new JLabel("© 2025 Computer Network project  Steal and Share", SwingConstants.RIGHT);
        footerLabel.setFont(new Font("Courier", Font.ITALIC, 12));
        controlPanel.add(footerLabel, BorderLayout.SOUTH);
        
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        // Button actions
        stealButton.addActionListener(e -> sendMove(GameConfig.STEAL));
        shareButton.addActionListener(e -> sendMove(GameConfig.SHARE));
        algorithmButton.addActionListener(e -> showAlgorithmDialog());
        readyButton.addActionListener(e -> {
            if (!isReady) {
                selectedRounds = (Integer) roundSelectionComboBox.getSelectedItem();
                isLongGame = selectedRounds == GameConfig.LONG_GAME_ROUNDS;
                isReady = true;
                readyButton.setEnabled(false);
                roundSelectionComboBox.setEnabled(false);
                out.println(String.format(GameConfig.ROUND_SELECTION, selectedRounds));
                out.println(String.format(GameConfig.READY_STATE, true));
                readyStatusLabel.setText("Ready: Yes");
                gameLog.append("You are ready to play " + selectedRounds + " rounds.\n");
            }
        });
        
        // Initialize algorithm timer (3-second delay)
        algorithmTimer = new Timer(3000, e -> {
            if (usingAlgorithm && selectedStrategy != null && currentRound > 0 && !moveSentForCurrentRound) {
                String move = selectedStrategy.getNextMove(opponentLastMove, currentRound);
                sendMove(move);
                gameLog.append("Algorithm (" + selectedStrategy.getName() + ") chose: " + move + "\n");
            }
        });
        algorithmTimer.setRepeats(false);
        
        // Disable buttons initially
        stealButton.setEnabled(false);
        shareButton.setEnabled(false);
        readyButton.setEnabled(true);
    }
    
    private void showRoundSelectionDialog() {
        JDialog dialog = new JDialog(this, "Game Setup", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Round selection
        JPanel roundPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel roundLabel = new JLabel("Select number of rounds:");
        roundSelectionComboBox = new JComboBox<>(new Integer[]{15, 200});
        roundSelectionComboBox.setSelectedItem(GameConfig.DEFAULT_ROUNDS);
        roundPanel.add(roundLabel);
        roundPanel.add(roundSelectionComboBox);
        
        // Ready button
        readyButton = new JButton("Ready");
        readyButton.addActionListener(e -> {
            selectedRounds = (Integer) roundSelectionComboBox.getSelectedItem();
            isLongGame = selectedRounds == GameConfig.LONG_GAME_ROUNDS;
            isReady = true;
            readyButton.setEnabled(false);
            roundSelectionComboBox.setEnabled(false);
            out.println(String.format(GameConfig.ROUND_SELECTION, selectedRounds));
            out.println(String.format(GameConfig.READY_STATE, true));
            dialog.dispose();
        });
        
        panel.add(roundPanel, BorderLayout.CENTER);
        panel.add(readyButton, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void showAlgorithmDialog() {
        // Only allow algorithm selection in the first round, before making a move
        if (currentRound > 1 || moveSentForCurrentRound) {
            JOptionPane.showMessageDialog(this, 
                "Algorithms can only be selected in the first round before making a move.",
                "Cannot Change Algorithm",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        JDialog dialog = new JDialog(this, "Select Algorithm", true);
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel label = new JLabel("Choose an algorithm to play for you:", SwingConstants.CENTER);
        label.setFont(new Font("Courier", Font.BOLD, 14));
        panel.add(label, BorderLayout.NORTH);
        
        // Radio buttons for algorithm selection
        JPanel radioPanel = new JPanel(new GridLayout(10, 1, 5, 5));
        ButtonGroup group = new ButtonGroup();
        
        JRadioButton titForTatButton = new JRadioButton("Tit for Tat (Start with SHARE, then copy opponent)");
        JRadioButton randomButton = new JRadioButton("Random (Randomly choose STEAL or SHARE)");
        JRadioButton systematicButton = new JRadioButton("Systematic (SHARE, STEAL, SHARE, STEAL, ...)");
        JRadioButton friedmanButton = new JRadioButton("Friedman (Start with SHARE, then always STEAL after opponent STEALS)");
        JRadioButton jossButton = new JRadioButton("Joss (Like Tit for Tat, but 10% chance to STEAL)");
        JRadioButton graaskampButton = new JRadioButton("Graaskamp (Like Tit for Tat, but STEALS after round 50)");
        JRadioButton titForTwoTatsButton = new JRadioButton("Tit for Two Tats (Only STEALS after two consecutive STEALS)");
        JRadioButton testerButton = new JRadioButton("Tester (Tests opponent with initial STEAL)");
        JRadioButton rakshithaButton = new JRadioButton("Rakshitha (First 10 moves random, then adapts)");
        
        group.add(titForTatButton);
        group.add(randomButton);
        group.add(systematicButton);
        group.add(friedmanButton);
        group.add(jossButton);
        group.add(graaskampButton);
        group.add(titForTwoTatsButton);
        group.add(testerButton);
        group.add(rakshithaButton);
        
        radioPanel.add(titForTatButton);
        radioPanel.add(randomButton);
        radioPanel.add(systematicButton);
        radioPanel.add(friedmanButton);
        radioPanel.add(jossButton);
        radioPanel.add(graaskampButton);
        radioPanel.add(titForTwoTatsButton);
        radioPanel.add(testerButton);
        radioPanel.add(rakshithaButton);
        
        panel.add(radioPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton selectButton = new JButton("Select Algorithm");
        JButton cancelButton = new JButton("Cancel");
        
        selectButton.addActionListener(e -> {
            if (titForTatButton.isSelected()) {
                selectedStrategy = new TitForTatStrategy();
            } else if (randomButton.isSelected()) {
                selectedStrategy = new RandomStrategy();
            } else if (systematicButton.isSelected()) {
                selectedStrategy = new SystematicStrategy();
            } else if (friedmanButton.isSelected()) {
                selectedStrategy = new FriedmanStrategy();
            } else if (jossButton.isSelected()) {
                selectedStrategy = new JossStrategy();
            } else if (graaskampButton.isSelected()) {
                selectedStrategy = new GraaskampStrategy();
            } else if (titForTwoTatsButton.isSelected()) {
                selectedStrategy = new TitForTwoTatsStrategy();
            } else if (testerButton.isSelected()) {
                selectedStrategy = new TesterStrategy();
            } else if (rakshithaButton.isSelected()) {
                selectedStrategy = new RakshithaStrategy();
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "Please select an algorithm.", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            usingAlgorithm = true;
            algorithmLabel.setText("Algorithm: " + selectedStrategy.getName());
            stealButton.setEnabled(false);
            shareButton.setEnabled(false);
            algorithmButton.setEnabled(false);
            
            gameLog.append("You selected algorithm: " + selectedStrategy.getName() + "\n");
            gameLog.append("The algorithm will now play the game for you.\n");
            
            // If we're already in a round, start the algorithm timer
            if (currentRound > 0 && !moveSentForCurrentRound) {
                gameLog.append("Algorithm is thinking for " + (isLongGame ? "0.5" : "3") + " seconds...\n");
                algorithmTimer.start();
            }
            
            dialog.dispose();
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(selectButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
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
            System.out.println("[CLIENT] Starting to listen for messages from server");
            while ((message = in.readLine()) != null) {
                System.out.println("[CLIENT] Received message from server: " + message);
                final String msg = message;
                SwingUtilities.invokeLater(() -> processMessage(msg));
            }
        } catch (IOException e) {
            System.err.println("[CLIENT] Error in message listener: " + e.getMessage());
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                gameLog.append("Connection lost.\n");
                stealButton.setEnabled(false);
                shareButton.setEnabled(false);
                algorithmButton.setEnabled(false);
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
            // Parse round number
            currentRound = Integer.parseInt(message.substring(6));
            roundLabel.setText(String.format("Round: %d/%d", currentRound, totalRounds));
            moveSentForCurrentRound = false;
            
            // Enable buttons if not using algorithm
            if (!usingAlgorithm) {
                stealButton.setEnabled(true);
                shareButton.setEnabled(true);
            } else {
                // Start algorithm timer with appropriate delay
                if (isLongGame) {
                    algorithmTimer.setInitialDelay((int) GameConfig.LONG_GAME_DELAY_MS);
                } else {
                    algorithmTimer.setInitialDelay(3000); // Default 3-second delay
                }
                algorithmTimer.start();
            }
        } else if (message.startsWith("RESULT:")) {
            // Parse result message
            String[] parts = message.substring(7).split(":");
            String myMove = parts[0];
            String opponentMove = parts[1];
            myCoins = Integer.parseInt(parts[2]);
            opponentCoins = Integer.parseInt(parts[3]);
            
            // Update UI
            coinsLabel.setText("Your Coins: " + myCoins);
            opponentCoinsLabel.setText("Opponent Coins: " + opponentCoins);
            
            // Store opponent's last move for algorithm
            opponentLastMove = opponentMove;
            
            // Play appropriate sound
            if (myMove.equals(GameConfig.STEAL) && opponentMove.equals(GameConfig.SHARE)) {
                playSound(stealSound);
            } else if (myMove.equals(GameConfig.SHARE) && opponentMove.equals(GameConfig.STEAL)) {
                playSound(loseSound);
            } else if (myMove.equals(GameConfig.SHARE) && opponentMove.equals(GameConfig.SHARE)) {
                playSound(shareSound);
            } else if (myMove.equals(GameConfig.STEAL) && opponentMove.equals(GameConfig.STEAL)) {
                playSound(bothStealSound);
            }
            
            // Log the result
            gameLog.append(String.format("Round %d: You %s, Opponent %s\n", 
                currentRound, myMove, opponentMove));
            gameLog.append(String.format("Your coins: %d, Opponent coins: %d\n", 
                myCoins, opponentCoins));
            
            // Disable buttons until next round
            stealButton.setEnabled(false);
            shareButton.setEnabled(false);
        } else if (message.equals(GameConfig.GAME_START)) {
            gameLog.append("Game started!\n");
            stealButton.setEnabled(true);
            shareButton.setEnabled(true);
            algorithmButton.setEnabled(false);
            roundSelectionComboBox.setEnabled(false);
        } else if (message.equals(GameConfig.GAME_OVER)) {
            gameLog.append("Game over!\n");
            stealButton.setEnabled(false);
            shareButton.setEnabled(false);
            algorithmButton.setEnabled(false);
        } else if (message.startsWith("FINAL_SUMMARY:")) {
            // Parse final summary
            String[] parts = message.split(":");
            if (parts.length == 9) {
                int myFinalCoins = Integer.parseInt(parts[1]);
                int opponentFinalCoins = Integer.parseInt(parts[2]);
                int myHits = Integer.parseInt(parts[3]);
                int myMisses = Integer.parseInt(parts[4]);
                double hitRate = Double.parseDouble(parts[5]);
                double missRate = Double.parseDouble(parts[6]);
                double noneRate = Double.parseDouble(parts[7]);
                String result = parts[8];
                
                // Add final summary to game log with nice formatting
                gameLog.append("\n\n=== FINAL GAME SUMMARY ===\n\n");
                gameLog.append(String.format("Your Final Coins: %d\n", myFinalCoins));
                gameLog.append(String.format("Opponent's Final Coins: %d\n", opponentFinalCoins));
                gameLog.append(String.format("Game Result: %s\n\n", result));
                
                gameLog.append("=== Detailed Statistics ===\n");
                gameLog.append(String.format("Total Hits: %d (%.2f%%)\n", myHits, hitRate));
                gameLog.append(String.format("Total Misses: %d (%.2f%%)\n", myMisses, missRate));
                gameLog.append(String.format("Total Draws: %.2f%%\n", noneRate));
                
                // Scroll to bottom
                gameLog.setCaretPosition(gameLog.getDocument().getLength());
            }
        } else if (message.startsWith("GAME_CONFIG:")) {
            // Parse final round configuration
            totalRounds = Integer.parseInt(message.substring(12));
            roundLabel.setText(String.format("Round: %d/%d", currentRound, totalRounds));
            gameLog.append(String.format("Game configured for %d rounds\n", totalRounds));
        } else if (message.startsWith("OPPONENT_READY:")) {
            boolean opponentReady = Boolean.parseBoolean(message.substring(14));
            gameLog.append("Opponent is " + (opponentReady ? "ready" : "not ready") + "\n");
        } else if (message.startsWith("STATS:")) {
            // Parse statistics
            String[] parts = message.split(":");
            if (parts.length == 4) {
                double hitRate = Double.parseDouble(parts[1]);
                double missRate = Double.parseDouble(parts[2]);
                double noneRate = Double.parseDouble(parts[3]);
                
                // Add statistics to game log
                gameLog.append("\n\nGame Statistics:\n");
                gameLog.append(String.format("Hit Rate: %.2f%% (Won when opponent shared)\n", hitRate));
                gameLog.append(String.format("Miss Rate: %.2f%% (Lost when opponent stole)\n", missRate));
                gameLog.append(String.format("None Rate: %.2f%% (Draws - both got same coins)\n", noneRate));
                
                // Scroll to bottom
                gameLog.setCaretPosition(gameLog.getDocument().getLength());
            }
        }
    }
    
    private void sendMove(String move) {
        System.out.println("[CLIENT] Sending move to server: " + move);
        out.println(move);
        out.flush(); // Ensure the move is sent immediately
        System.out.println("[CLIENT] Move sent and output flushed");
        
        stealButton.setEnabled(false);
        shareButton.setEnabled(false);
        algorithmButton.setEnabled(false);
        moveSentForCurrentRound = true; // Mark that we've sent a move for this round
        
        // Just inform the player about their choice - don't show opponent's choice
        gameLog.append("You selected: " + move + "\n");
        gameLog.append("Waiting for round to complete...\n");
        System.out.println("[CLIENT] Updated UI after sending move");
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