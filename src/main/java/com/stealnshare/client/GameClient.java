package com.stealnshare.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
    private JLabel titleLabel, footerLabel, balanceLabel;
    private JPanel mainPanel;
    
    public GameClient() {
        setupUI();
        connectToServer();
    }
    
    private void setupUI() {
        // Basic window setup
        setTitle("Steal and Share - 1990s Retro Edition");
        setSize(600, 500);
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
        
        // Control panel for buttons and balance
        JPanel controlPanel = new JPanel(new BorderLayout(10, 10));
        
        // Balance display
        balanceLabel = new JLabel("Balance: $0", SwingConstants.CENTER);
        balanceLabel.setFont(new Font("Courier", Font.BOLD, 16));
        controlPanel.add(balanceLabel, BorderLayout.NORTH);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        stealButton = createRetroButton("STEAL", new Color(200, 0, 0));
        shareButton = createRetroButton("SHARE", new Color(0, 100, 0));
        buttonPanel.add(stealButton);
        buttonPanel.add(shareButton);
        controlPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // Footer
        footerLabel = new JLabel("Cn lab project", SwingConstants.RIGHT);
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
            int numRounds = Integer.parseInt(numRoundsStr);
            
            // Connect to server
            socket = new Socket("localhost", GameConfig.PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // Send configuration
            out.println(String.format(GameConfig.CONFIG_FORMAT, totalMoney, numRounds));
            balanceLabel.setText("Balance: $" + totalMoney);
            
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
    
    private void processMessage(String message) {
        gameLog.append(message + "\n");
        gameLog.setCaretPosition(gameLog.getDocument().getLength());
        
        if (message.startsWith("ROUND:")) {
            stealButton.setEnabled(true);
            shareButton.setEnabled(true);
        } else if (message.startsWith("RESULT:")) {
            stealButton.setEnabled(false);
            shareButton.setEnabled(false);
            // Update balance from result message
            String[] parts = message.split("\\$");
            if (parts.length > 1) {
                balanceLabel.setText("Balance: $" + parts[1]);
            }
        } else if (message.startsWith("GAME_OVER:")) {
            stealButton.setEnabled(false);
            shareButton.setEnabled(false);
        }
    }
    
    private void sendMove(String move) {
        if (out != null) {
            out.println(move);
            stealButton.setEnabled(false);
            shareButton.setEnabled(false);
            gameLog.append("You chose: " + move + "\n");
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new GameClient().setVisible(true);
        });
    }
}
