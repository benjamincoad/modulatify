package com.modulatify;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.security.SecureRandom;

public class ConfigManager {
    private static final Logger logger = Logger.getLogger(ConfigManager.class.getName());
    
    private static final String CONFIG_DIR = System.getProperty("user.home") + "\\AppData\\Roaming\\Modulatify";
    private static final String CONFIG_FILE = CONFIG_DIR + "\\config.properties";
    private static final String KEY_FILE = CONFIG_DIR + "\\key.dat";
    
    private Properties config;
    private SecretKey encryptionKey;
    
    public ConfigManager() {
        this.config = new Properties();
        initializeEncryptionKey();
        setDefaultValues();
    }
    
    private void initializeEncryptionKey() {
        try {
            Path keyPath = Paths.get(KEY_FILE);
            if (Files.exists(keyPath)) {
                byte[] keyBytes = Files.readAllBytes(keyPath);
                encryptionKey = new SecretKeySpec(keyBytes, "AES");
            } else {
                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(256);
                encryptionKey = keyGen.generateKey();
                
                Files.createDirectories(Paths.get(CONFIG_DIR));
                Files.write(keyPath, encryptionKey.getEncoded());
            }
        } catch (Exception e) {
            logger.severe("Failed to initialize encryption key: " + e.getMessage());
            throw new RuntimeException("Encryption initialization failed", e);
        }
    }
    
    private void setDefaultValues() {
        config.setProperty("hotkey.skip_forward", "Ctrl+Alt+O");
        config.setProperty("hotkey.skip_backward", "Ctrl+Alt+I");
        config.setProperty("hotkey.play_pause", "Ctrl+Alt+P");
        config.setProperty("hotkey.volume_down", "Ctrl+Alt+K");
        config.setProperty("hotkey.volume_up", "Ctrl+Alt+L");
        config.setProperty("spotify.access_token", "");
        config.setProperty("spotify.refresh_token", "");
        config.setProperty("spotify.token_expires_at", "0");
    }
    
    public void loadConfig() {
        try {
            Path configPath = Paths.get(CONFIG_FILE);
            if (Files.exists(configPath)) {
                try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
                    config.load(fis);
                    logger.info("Configuration loaded successfully");
                }
            } else {
                logger.info("No existing configuration found, using defaults");
            }
        } catch (IOException e) {
            logger.warning("Failed to load configuration: " + e.getMessage());
        }
    }
    
    public void saveConfig() throws IOException {
        Files.createDirectories(Paths.get(CONFIG_DIR));
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            config.store(fos, "Modulatify Configuration");
            logger.info("Configuration saved successfully");
        }
    }
    
    public String getHotkey(String action) {
        return config.getProperty("hotkey." + action);
    }
    
    public void setHotkey(String action, String hotkey) {
        config.setProperty("hotkey." + action, hotkey);
    }
    
    public String getSpotifyAccessToken() {
        String encryptedToken = config.getProperty("spotify.access_token");
        if (encryptedToken == null || encryptedToken.isEmpty()) {
            return "";
        }
        return decryptToken(encryptedToken);
    }
    
    public void setSpotifyAccessToken(String token) {
        if (token == null || token.isEmpty()) {
            config.setProperty("spotify.access_token", "");
        } else {
            config.setProperty("spotify.access_token", encryptToken(token));
        }
    }
    
    public String getSpotifyRefreshToken() {
        String encryptedToken = config.getProperty("spotify.refresh_token");
        if (encryptedToken == null || encryptedToken.isEmpty()) {
            return "";
        }
        return decryptToken(encryptedToken);
    }
    
    public void setSpotifyRefreshToken(String token) {
        if (token == null || token.isEmpty()) {
            config.setProperty("spotify.refresh_token", "");
        } else {
            config.setProperty("spotify.refresh_token", encryptToken(token));
        }
    }
    
    public long getTokenExpiresAt() {
        return Long.parseLong(config.getProperty("spotify.token_expires_at", "0"));
    }
    
    public void setTokenExpiresAt(long expiresAt) {
        config.setProperty("spotify.token_expires_at", String.valueOf(expiresAt));
    }
    
    private String encryptToken(String token) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
            byte[] encrypted = cipher.doFinal(token.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            logger.severe("Failed to encrypt token: " + e.getMessage());
            return token;
        }
    }
    
    private String decryptToken(String encryptedToken) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedToken));
            return new String(decrypted);
        } catch (Exception e) {
            logger.severe("Failed to decrypt token: " + e.getMessage());
            return "";
        }
    }
}