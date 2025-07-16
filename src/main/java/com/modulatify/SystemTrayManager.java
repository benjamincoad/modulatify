package com.modulatify;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class SystemTrayManager {
    private static final Logger logger = Logger.getLogger(SystemTrayManager.class.getName());
    
    private final ModulatifyApp app;
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    
    public SystemTrayManager(ModulatifyApp app) {
        this.app = app;
        this.systemTray = SystemTray.getSystemTray();
    }
    
    public void createTrayIcon() throws Exception {
        Image iconImage = loadTrayIcon();
        
        PopupMenu popup = new PopupMenu();
        
        MenuItem settingsItem = new MenuItem("Settings");
        settingsItem.addActionListener(e -> app.showSettings());
        popup.add(settingsItem);
        
        popup.addSeparator();
        
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> app.shutdown());
        popup.add(exitItem);
        
        trayIcon = new TrayIcon(iconImage, "Modulatify", popup);
        trayIcon.setImageAutoSize(true);
        
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    app.showSettings();
                }
            }
        });
        
        systemTray.add(trayIcon);
        logger.info("System tray icon created successfully");
    }
    
    private Image loadTrayIcon() {
        try {
            InputStream iconStream = getClass().getResourceAsStream("/icons/modulatify_icon.png");
            if (iconStream == null) {
                logger.warning("Icon not found, using default icon");
                return createDefaultIcon();
            }
            return Toolkit.getDefaultToolkit().createImage(iconStream.readAllBytes());
        } catch (IOException e) {
            logger.warning("Failed to load icon, using default: " + e.getMessage());
            return createDefaultIcon();
        }
    }
    
    private Image createDefaultIcon() {
        int size = 16;
        Image image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = ((BufferedImage) image).createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(new Color(30, 215, 96));
        g2d.fillOval(2, 2, size - 4, size - 4);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "M";
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        g2d.drawString(text, (size - textWidth) / 2, (size + textHeight) / 2 - 2);
        
        g2d.dispose();
        return image;
    }
    
    public void showMessage(String caption, String text, TrayIcon.MessageType messageType) {
        if (trayIcon != null) {
            trayIcon.displayMessage(caption, text, messageType);
        }
    }
    
    public void removeTrayIcon() {
        if (trayIcon != null) {
            systemTray.remove(trayIcon);
        }
    }
}