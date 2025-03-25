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
    private JLabel titleLabel, footerLabel, balanceLabel, roundLabel, opponentLabel;
    private JPanel mainPanel, statsPanel;
    private int currentRound = 0;
    private int totalRounds = 0;
    private int myBalance = 0;
    private int opponentBalance = 0;
    
    // Sound clips
    private Clip stealSound;
    private Clip shareSound;
    private Clip loseSound;
    private Clip bothStealSound;
    
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
        roundLabel = new JLabel("Round: 0/0", SwingConstants.CENTER);
        roundLabel.setFont(new Font("Courier", Font.BOLD, 16));
        statsPanel.add(roundLabel);
        
        // Balance display
        balanceLabel = new JLabel("Your Balance: $0", SwingConstants.CENTER);
        balanceLabel.setFont(new Font("Courier", Font.BOLD, 16));
        statsPanel.add(balanceLabel);
        
        // Opponent balance
        opponentLabel = new JLabel("Opponent Balance: $0", SwingConstants.CENTER);
        opponentLabel.setFont(new Font("Courier", Font.BOLD, 16));
        statsPanel.add(opponentLabel);
        
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
        footerLabel = new JLabel("© 2024 Steal and Share", SwingConstants.RIGHT);
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
            // Get initial configuration from user
            String totalMoneyStr = JOptionPane.showInputDialog(this, 
                "Enter total money:", "Game Configuration", 
                JOptionPane.QUESTION_MESSAGE);
            String numRoundsStr = JOptionPane.showInputDialog(this, 
                "Enter number of rounds:", "Game Configuration", 
                JOptionPane.QUESTION_MESSAGE);
                
            if (totalMoneyStr == null || numRoundsStr == null) {
                System.exit(0);
            }
            
            int totalMoney = Integer.parseInt(totalMoneyStr);
            totalRounds = Integer.parseInt(numRoundsStr);
            
            // Connect to server
            socket = new Socket("localhost", GameConfig.PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // Send configuration
            out.println(String.format(GameConfig.CONFIG_FORMAT, totalMoney, totalRounds));
            myBalance = totalMoney;
            opponentBalance = totalMoney;
            balanceLabel.setText(String.format("Your Balance: $%d", myBalance));
            opponentLabel.setText(String.format("Opponent Balance: $%d", opponentBalance));
            
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
        gameLog.append(message + "\n");
        gameLog.setCaretPosition(gameLog.getDocument().getLength());
        
        if (message.startsWith("ROUND:")) {
            currentRound = Integer.parseInt(message.split(":")[1]);
            roundLabel.setText(String.format("Round: %d/%d", currentRound, totalRounds));
            stealButton.setEnabled(true);
            shareButton.setEnabled(true);
        } else if (message.startsWith("RESULT:")) {
            stealButton.setEnabled(false);
            shareButton.setEnabled(false);
            
            // Parse result message
            String[] parts = message.split(":");
            String player1Move = parts[1];
            String player2Move = parts[2];
            int player1Balance = Integer.parseInt(parts[3]);
            int player2Balance = Integer.parseInt(parts[4]);
            
            // Update balances
            myBalance = player1Balance;
            opponentBalance = player2Balance;
            balanceLabel.setText(String.format("Your Balance: $%d", myBalance));
            opponentLabel.setText(String.format("Opponent Balance: $%d", opponentBalance));
            
            // Format and display round result
            String resultMessage = String.format("\nRound %d Result:\n", currentRound);
            resultMessage += String.format("You chose: %s\n", player1Move);
            resultMessage += String.format("Opponent chose: %s\n", player2Move);
            resultMessage += String.format("Your balance changed: $%d\n", player1Balance - myBalance);
            resultMessage += String.format("Opponent's balance changed: $%d\n", player2Balance - opponentBalance);
            gameLog.append(resultMessage);
            
            // Play appropriate sound based on the result
            if (player1Move.equals(GameConfig.STEAL) && player2Move.equals(GameConfig.SHARE)) {
                playSound(stealSound);
            } else if (player1Move.equals(GameConfig.SHARE) && player2Move.equals(GameConfig.SHARE)) {
                playSound(shareSound);
            } else if (player1Move.equals(GameConfig.STEAL) && player2Move.equals(GameConfig.STEAL)) {
                playSound(bothStealSound);
            } else if (player1Move.equals(GameConfig.SHARE) && player2Move.equals(GameConfig.STEAL)) {
                playSound(loseSound);
            }
        } else if (message.startsWith("GAME_OVER:")) {
            stealButton.setEnabled(false);
            shareButton.setEnabled(false);
            gameLog.append("\nGame Over!\n");
            gameLog.append(String.format("Final Balance: $%d\n", myBalance));
            gameLog.append(String.format("Opponent's Final Balance: $%d\n", opponentBalance));
        }
    }
    
    private void sendMove(String move) {
        out.println(move);
        stealButton.setEnabled(false);
        shareButton.setEnabled(false);
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