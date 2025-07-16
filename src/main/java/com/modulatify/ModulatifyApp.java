package com.modulatify;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

public class ModulatifyApp {
    private static final Logger logger = Logger.getLogger(ModulatifyApp.class.getName());
    
    private SystemTrayManager trayManager;
    private HotkeyManager hotkeyManager;
    private SpotifyController spotifyController;
    private ConfigManager configManager;
    private SettingsGUI settingsGUI;
    
    public static void main(String[] args) {
        if (!SystemTray.isSupported()) {
            System.err.println("System tray is not supported on this platform");
            System.exit(1);
        }
        
        System.setProperty("java.awt.headless", "false");
        
        SwingUtilities.invokeLater(() -> {
            try {
                new ModulatifyApp().start();
            } catch (Exception e) {
                logger.severe("Failed to start Modulatify: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
    
    private void start() throws Exception {
        logger.info("Starting Modulatify...");
        
        configManager = new ConfigManager();
        configManager.loadConfig();
        
        spotifyController = new SpotifyController(configManager);
        
        hotkeyManager = new HotkeyManager(spotifyController, configManager);
        hotkeyManager.registerHotkeys();
        
        trayManager = new SystemTrayManager(this);
        trayManager.createTrayIcon();
        
        logger.info("Modulatify started successfully");
    }
    
    public void showSettings() {
        if (settingsGUI == null) {
            settingsGUI = new SettingsGUI(this, configManager, spotifyController);
        }
        settingsGUI.setVisible(true);
    }
    
    public void applySettings() {
        try {
            configManager.saveConfig();
            hotkeyManager.updateHotkeys();
            logger.info("Settings applied successfully");
        } catch (Exception e) {
            logger.severe("Failed to apply settings: " + e.getMessage());
        }
    }
    
    public void shutdown() {
        logger.info("Shutting down Modulatify...");
        
        if (hotkeyManager != null) {
            hotkeyManager.cleanup();
        }
        
        if (configManager != null) {
            try {
                configManager.saveConfig();
            } catch (Exception e) {
                logger.warning("Failed to save config on shutdown: " + e.getMessage());
            }
        }
        
        System.exit(0);
    }
}