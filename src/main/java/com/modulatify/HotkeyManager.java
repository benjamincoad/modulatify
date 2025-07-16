package com.modulatify;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;

public class HotkeyManager implements NativeKeyListener {
    private static final Logger logger = Logger.getLogger(HotkeyManager.class.getName());
    
    private final SpotifyController spotifyController;
    private final ConfigManager configManager;
    private final Map<String, Runnable> hotkeyActions;
    private final Map<String, Integer> keyCodeMap;
    private boolean enabled = true;
    
    public HotkeyManager(SpotifyController spotifyController, ConfigManager configManager) {
        this.spotifyController = spotifyController;
        this.configManager = configManager;
        this.hotkeyActions = new ConcurrentHashMap<>();
        this.keyCodeMap = new HashMap<>();
        
        initializeKeyCodeMap();
        initializeHotkeyActions();
    }
    
    private void initializeKeyCodeMap() {
        keyCodeMap.put("A", NativeKeyEvent.VC_A);
        keyCodeMap.put("B", NativeKeyEvent.VC_B);
        keyCodeMap.put("C", NativeKeyEvent.VC_C);
        keyCodeMap.put("D", NativeKeyEvent.VC_D);
        keyCodeMap.put("E", NativeKeyEvent.VC_E);
        keyCodeMap.put("F", NativeKeyEvent.VC_F);
        keyCodeMap.put("G", NativeKeyEvent.VC_G);
        keyCodeMap.put("H", NativeKeyEvent.VC_H);
        keyCodeMap.put("I", NativeKeyEvent.VC_I);
        keyCodeMap.put("J", NativeKeyEvent.VC_J);
        keyCodeMap.put("K", NativeKeyEvent.VC_K);
        keyCodeMap.put("L", NativeKeyEvent.VC_L);
        keyCodeMap.put("M", NativeKeyEvent.VC_M);
        keyCodeMap.put("N", NativeKeyEvent.VC_N);
        keyCodeMap.put("O", NativeKeyEvent.VC_O);
        keyCodeMap.put("P", NativeKeyEvent.VC_P);
        keyCodeMap.put("Q", NativeKeyEvent.VC_Q);
        keyCodeMap.put("R", NativeKeyEvent.VC_R);
        keyCodeMap.put("S", NativeKeyEvent.VC_S);
        keyCodeMap.put("T", NativeKeyEvent.VC_T);
        keyCodeMap.put("U", NativeKeyEvent.VC_U);
        keyCodeMap.put("V", NativeKeyEvent.VC_V);
        keyCodeMap.put("W", NativeKeyEvent.VC_W);
        keyCodeMap.put("X", NativeKeyEvent.VC_X);
        keyCodeMap.put("Y", NativeKeyEvent.VC_Y);
        keyCodeMap.put("Z", NativeKeyEvent.VC_Z);
        
        keyCodeMap.put("0", NativeKeyEvent.VC_0);
        keyCodeMap.put("1", NativeKeyEvent.VC_1);
        keyCodeMap.put("2", NativeKeyEvent.VC_2);
        keyCodeMap.put("3", NativeKeyEvent.VC_3);
        keyCodeMap.put("4", NativeKeyEvent.VC_4);
        keyCodeMap.put("5", NativeKeyEvent.VC_5);
        keyCodeMap.put("6", NativeKeyEvent.VC_6);
        keyCodeMap.put("7", NativeKeyEvent.VC_7);
        keyCodeMap.put("8", NativeKeyEvent.VC_8);
        keyCodeMap.put("9", NativeKeyEvent.VC_9);
        
        keyCodeMap.put("F1", NativeKeyEvent.VC_F1);
        keyCodeMap.put("F2", NativeKeyEvent.VC_F2);
        keyCodeMap.put("F3", NativeKeyEvent.VC_F3);
        keyCodeMap.put("F4", NativeKeyEvent.VC_F4);
        keyCodeMap.put("F5", NativeKeyEvent.VC_F5);
        keyCodeMap.put("F6", NativeKeyEvent.VC_F6);
        keyCodeMap.put("F7", NativeKeyEvent.VC_F7);
        keyCodeMap.put("F8", NativeKeyEvent.VC_F8);
        keyCodeMap.put("F9", NativeKeyEvent.VC_F9);
        keyCodeMap.put("F10", NativeKeyEvent.VC_F10);
        keyCodeMap.put("F11", NativeKeyEvent.VC_F11);
        keyCodeMap.put("F12", NativeKeyEvent.VC_F12);
        
        keyCodeMap.put("SPACE", NativeKeyEvent.VC_SPACE);
        keyCodeMap.put("ENTER", NativeKeyEvent.VC_ENTER);
        keyCodeMap.put("ESCAPE", NativeKeyEvent.VC_ESCAPE);
        keyCodeMap.put("TAB", NativeKeyEvent.VC_TAB);
        keyCodeMap.put("BACKSPACE", NativeKeyEvent.VC_BACKSPACE);
        keyCodeMap.put("DELETE", NativeKeyEvent.VC_DELETE);
        keyCodeMap.put("INSERT", NativeKeyEvent.VC_INSERT);
        keyCodeMap.put("HOME", NativeKeyEvent.VC_HOME);
        keyCodeMap.put("END", NativeKeyEvent.VC_END);
        keyCodeMap.put("PAGE_UP", NativeKeyEvent.VC_PAGE_UP);
        keyCodeMap.put("PAGE_DOWN", NativeKeyEvent.VC_PAGE_DOWN);
        keyCodeMap.put("UP", NativeKeyEvent.VC_UP);
        keyCodeMap.put("DOWN", NativeKeyEvent.VC_DOWN);
        keyCodeMap.put("LEFT", NativeKeyEvent.VC_LEFT);
        keyCodeMap.put("RIGHT", NativeKeyEvent.VC_RIGHT);
    }
    
    private void initializeHotkeyActions() {
        hotkeyActions.put("skip_forward", () -> spotifyController.skipForward());
        hotkeyActions.put("skip_backward", () -> spotifyController.skipBackward());
        hotkeyActions.put("play_pause", () -> spotifyController.togglePlayPause());
        hotkeyActions.put("volume_down", () -> spotifyController.volumeDown());
        hotkeyActions.put("volume_up", () -> spotifyController.volumeUp());
    }
    
    public void registerHotkeys() throws NativeHookException {
        GlobalScreen.registerNativeHook();
        GlobalScreen.addNativeKeyListener(this);
        logger.info("Global hotkeys registered successfully");
    }
    
    public void updateHotkeys() {
        logger.info("Hotkey configuration updated");
    }
    
    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (!enabled) {
            return;
        }
        
        String pressedKey = getKeyString(e);
        
        for (String action : hotkeyActions.keySet()) {
            String configuredHotkey = configManager.getHotkey(action);
            if (configuredHotkey != null && configuredHotkey.equals(pressedKey)) {
                logger.info("Hotkey activated: " + action + " (" + pressedKey + ")");
                hotkeyActions.get(action).run();
                break;
            }
        }
    }
    
    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
    }
    
    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
    }
    
    private String getKeyString(NativeKeyEvent e) {
        StringBuilder keyString = new StringBuilder();
        
        if ((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0) {
            keyString.append("Ctrl+");
        }
        if ((e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0) {
            keyString.append("Alt+");
        }
        if ((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0) {
            keyString.append("Shift+");
        }
        if ((e.getModifiers() & NativeKeyEvent.META_MASK) != 0) {
            keyString.append("Meta+");
        }
        
        String keyText = NativeKeyEvent.getKeyText(e.getKeyCode());
        keyString.append(keyText);
        
        return keyString.toString();
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        logger.info("Hotkeys " + (enabled ? "enabled" : "disabled"));
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void cleanup() {
        try {
            GlobalScreen.removeNativeKeyListener(this);
            GlobalScreen.unregisterNativeHook();
            logger.info("Global hotkeys unregistered successfully");
        } catch (NativeHookException e) {
            logger.warning("Failed to unregister global hotkeys: " + e.getMessage());
        }
    }
}