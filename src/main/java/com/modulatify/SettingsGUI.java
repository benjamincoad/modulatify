package com.modulatify;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SettingsGUI extends JFrame {
    private static final Logger logger = Logger.getLogger(SettingsGUI.class.getName());
    
    private final ModulatifyApp app;
    private final ConfigManager configManager;
    private final SpotifyController spotifyController;
    
    private final Map<String, JTextField> hotkeyFields;
    private JLabel spotifyStatusLabel;
    private JButton spotifyButton;
    private JButton applyButton;
    private JButton cancelButton;
    
    public SettingsGUI(ModulatifyApp app, ConfigManager configManager, SpotifyController spotifyController) {
        this.app = app;
        this.configManager = configManager;
        this.spotifyController = spotifyController;
        this.hotkeyFields = new HashMap<>();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        updateSpotifyStatus();
        loadCurrentSettings();
        
        setTitle("Modulatify Settings");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }
    
    private void initializeComponents() {
        hotkeyFields.put("skip_forward", new JTextField(15));
        hotkeyFields.put("skip_backward", new JTextField(15));
        hotkeyFields.put("play_pause", new JTextField(15));
        hotkeyFields.put("volume_down", new JTextField(15));
        hotkeyFields.put("volume_up", new JTextField(15));
        
        spotifyStatusLabel = new JLabel("Disconnected");
        spotifyButton = new JButton("Connect");
        applyButton = new JButton("Apply");
        cancelButton = new JButton("Cancel");
        
        for (JTextField field : hotkeyFields.values()) {
            field.setEditable(false);
            field.setFocusable(true);
        }
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Skip Forward:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(hotkeyFields.get("skip_forward"), gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Skip Backward:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(hotkeyFields.get("skip_backward"), gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Play/Pause:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(hotkeyFields.get("play_pause"), gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Volume Down:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(hotkeyFields.get("volume_down"), gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("Volume Up:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(hotkeyFields.get("volume_up"), gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        mainPanel.add(new JSeparator(), gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Spotify:"), gbc);
        gbc.gridx = 1;
        JPanel spotifyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        spotifyPanel.add(spotifyStatusLabel);
        spotifyPanel.add(Box.createHorizontalStrut(10));
        spotifyPanel.add(spotifyButton);
        mainPanel.add(spotifyPanel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        mainPanel.add(new JSeparator(), gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        for (Map.Entry<String, JTextField> entry : hotkeyFields.entrySet()) {
            final String action = entry.getKey();
            final JTextField field = entry.getValue();
            
            field.addKeyListener(new KeyListener() {
                @Override
                public void keyPressed(KeyEvent e) {
                    String keyString = buildKeyString(e);
                    field.setText(keyString);
                    e.consume();
                }
                
                @Override
                public void keyReleased(KeyEvent e) {
                    e.consume();
                }
                
                @Override
                public void keyTyped(KeyEvent e) {
                    e.consume();
                }
            });
            
            field.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    field.selectAll();
                }
            });
        }
        
        spotifyButton.addActionListener(e -> handleSpotifyConnection());
        
        applyButton.addActionListener(e -> applySettings());
        
        cancelButton.addActionListener(e -> {
            loadCurrentSettings();
            setVisible(false);
        });
    }
    
    private String buildKeyString(KeyEvent e) {
        StringBuilder keyString = new StringBuilder();
        
        if (e.isControlDown()) {
            keyString.append("Ctrl+");
        }
        if (e.isAltDown()) {
            keyString.append("Alt+");
        }
        if (e.isShiftDown()) {
            keyString.append("Shift+");
        }
        if (e.isMetaDown()) {
            keyString.append("Meta+");
        }
        
        int keyCode = e.getKeyCode();
        String keyText = KeyEvent.getKeyText(keyCode);
        
        if (keyCode != KeyEvent.VK_CONTROL && keyCode != KeyEvent.VK_ALT && 
            keyCode != KeyEvent.VK_SHIFT && keyCode != KeyEvent.VK_META) {
            keyString.append(keyText);
        }
        
        return keyString.toString();
    }
    
    private void handleSpotifyConnection() {
        if (spotifyController.isAuthenticated()) {
            int result = JOptionPane.showConfirmDialog(this, 
                "You are already connected to Spotify. Do you want to reconnect?", 
                "Spotify Connection", 
                JOptionPane.YES_NO_OPTION);
            
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        try {
            String authUrl = spotifyController.getAuthorizationUrl();
            Desktop.getDesktop().browse(new URI(authUrl));
            
            String code = JOptionPane.showInputDialog(this, 
                "Please authorize Modulatify in your browser and paste the authorization code here:");
            
            if (code != null && !code.trim().isEmpty()) {
                if (spotifyController.exchangeCodeForTokens(code.trim())) {
                    JOptionPane.showMessageDialog(this, "Successfully connected to Spotify!");
                    updateSpotifyStatus();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to connect to Spotify. Please try again.", 
                        "Connection Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            logger.severe("Error during Spotify connection: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error opening browser. Please try again.", 
                "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateSpotifyStatus() {
        if (spotifyController.isAuthenticated()) {
            spotifyStatusLabel.setText("Connected ✓");
            spotifyStatusLabel.setForeground(new Color(0, 150, 0));
            spotifyButton.setText("Reconnect");
        } else {
            spotifyStatusLabel.setText("Disconnected");
            spotifyStatusLabel.setForeground(Color.RED);
            spotifyButton.setText("Connect");
        }
    }
    
    private void loadCurrentSettings() {
        hotkeyFields.get("skip_forward").setText(configManager.getHotkey("skip_forward"));
        hotkeyFields.get("skip_backward").setText(configManager.getHotkey("skip_backward"));
        hotkeyFields.get("play_pause").setText(configManager.getHotkey("play_pause"));
        hotkeyFields.get("volume_down").setText(configManager.getHotkey("volume_down"));
        hotkeyFields.get("volume_up").setText(configManager.getHotkey("volume_up"));
    }
    
    private void applySettings() {
        if (!validateSettings()) {
            return;
        }
        
        configManager.setHotkey("skip_forward", hotkeyFields.get("skip_forward").getText());
        configManager.setHotkey("skip_backward", hotkeyFields.get("skip_backward").getText());
        configManager.setHotkey("play_pause", hotkeyFields.get("play_pause").getText());
        configManager.setHotkey("volume_down", hotkeyFields.get("volume_down").getText());
        configManager.setHotkey("volume_up", hotkeyFields.get("volume_up").getText());
        
        app.applySettings();
        
        JOptionPane.showMessageDialog(this, "Settings applied successfully!");
        setVisible(false);
    }
    
    private boolean validateSettings() {
        java.util.Set<String> usedHotkeys = new java.util.HashSet<>();
        
        for (Map.Entry<String, JTextField> entry : hotkeyFields.entrySet()) {
            String hotkey = entry.getValue().getText().trim();
            
            if (hotkey.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please set a hotkey for " + entry.getKey().replace("_", " "), 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (usedHotkeys.contains(hotkey)) {
                JOptionPane.showMessageDialog(this, 
                    "Duplicate hotkey detected: " + hotkey, 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            usedHotkeys.add(hotkey);
        }
        
        return true;
    }
}