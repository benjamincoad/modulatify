# Modulatify - Windows Spotify Control App Specification

## Overview
Modulatify is a Windows background application that provides global hotkey control for Spotify playback functions. Built in Java, it runs minimized to the system tray and allows users to control Spotify from any application.

## Core Features

### Hotkey Controls
- **Skip Forward**: Default `Ctrl + Alt + O`
- **Skip Backward**: Default `Ctrl + Alt + I`
- **Play/Pause**: Default `Ctrl + Alt + P`
- **Volume Down**: Default `Ctrl + Alt + K`
- **Volume Up**: Default `Ctrl + Alt + L`

### Global Hotkey Behavior
- Hotkeys work globally across all applications
- Hotkeys are disabled when computer is locked or screensaver is active
- Support for both single keys and key combinations
- No restrictions on key assignments (user can choose any key/combination)

## Technical Architecture

### Java Implementation
- Target Platform: Windows 10/11
- Java Version: Any modern version (8+)
- Architecture: Background service with system tray integration

### Spotify Integration
- **API**: Spotify Web API for all playback controls
- **Authentication**: OAuth 2.0 flow with automatic token refresh
- **Scopes Required**: 
  - `user-read-playback-state`
  - `user-modify-playback-state`
- **Token Management**: Automatic refresh handling in background

### Required Java Libraries
- **JNativeHook**: For global hotkey capture
- **System Tray**: Java AWT SystemTray for tray icon
- **HTTP Client**: For Spotify API calls (Java 11+ HttpClient or OkHttp)
- **JSON Processing**: For API response handling
- **Windows Registry**: For auto-start functionality

## User Interface

### System Tray Icon
- **Icon File**: `modulatify_icon.ico` (16x16 and 32x32 sizes)
- **Location**: Place in `src/main/resources/icons/` directory
- **Behavior**:
  - Left-click: Open settings GUI
  - Right-click: Context menu with "Settings" and "Exit"
- **States**: Single static icon (no state changes)

### Settings GUI
- **Window Type**: Simple dialog window (not resizable)
- **Access**: Only through system tray (no Start menu shortcut)
- **Layout**: Vertical form with labeled input fields
- **Components**:
  - 5 hotkey assignment fields with labels
  - Spotify connection status/button
  - Apply/Save button
  - Cancel button

### Settings Window Layout
```
┌─────────────────────────────────────┐
│            Modulatify Settings        │
├─────────────────────────────────────┤
│ Skip Forward:     [Ctrl+Alt+O    ] │
│ Skip Backward:    [Ctrl+Alt+I    ] │
│ Play/Pause:       [Ctrl+Alt+P    ] │
│ Volume Down:      [Ctrl+Alt+K    ] │
│ Volume Up:        [Ctrl+Alt+L    ] │
├─────────────────────────────────────┤
│ Spotify: [Connected ✓] [Reconnect] │
├─────────────────────────────────────┤
│              [Apply] [Cancel]       │
└─────────────────────────────────────┘
```

## Configuration Management

### Settings Storage
- **Format**: Properties file or simple JSON
- **Location**: `%APPDATA%/Modulatify/config.properties`
- **Contents**:
  - 5 hotkey mappings
  - Spotify access token (encrypted)
  - Spotify refresh token (encrypted)

### Default Configuration
```properties
hotkey.skip_forward=Ctrl+Alt+O
hotkey.skip_backward=Ctrl+Alt+I
hotkey.play_pause=Ctrl+Alt+P
hotkey.volume_down=Ctrl+Alt+K
hotkey.volume_up=Ctrl+Alt+L
spotify.access_token=<encrypted>
spotify.refresh_token=<encrypted>
```

## Spotify API Integration

### Authentication Flow
1. **First Launch**: Automatic OAuth flow opens browser
2. **User Authorization**: User logs into Spotify and authorizes app
3. **Token Storage**: Access and refresh tokens stored encrypted
4. **Background Refresh**: Tokens refreshed automatically before expiration

### API Endpoints Used
- `GET /v1/me/player` - Get current playback state
- `POST /v1/me/player/next` - Skip to next track
- `POST /v1/me/player/previous` - Skip to previous track
- `PUT /v1/me/player/play` - Resume playback
- `PUT /v1/me/player/pause` - Pause playback
- `PUT /v1/me/player/volume` - Set volume level

### Error Handling
- **No Spotify Running**: Hotkeys do nothing (silent failure)
- **API Errors**: Log to console, continue operation
- **Token Expiry**: Automatic refresh attempt
- **Network Issues**: Retry with exponential backoff

## Installation & Deployment

### Installer Requirements
- **Type**: Windows MSI installer
- **Auto-start**: Registry entry for Windows startup
- **Location**: `HKEY_CURRENT_USER\SOFTWARE\Microsoft\Windows\CurrentVersion\Run`
- **Uninstaller**: Standard Windows uninstall process

### File Structure
```
Modulatify/
├── modulatify.jar
├── icons/
│   └── modulatify_icon.ico
├── lib/ (if using external JARs)
└── uninstall.exe
```

### Registry Entry
- **Key**: `HKEY_CURRENT_USER\SOFTWARE\Microsoft\Windows\CurrentVersion\Run`
- **Value**: `Modulatify`
- **Data**: `"C:\Program Files\Modulatify\modulatify.jar"`

## Security Considerations

### Token Security
- Encrypt stored Spotify tokens using Windows DPAPI
- No plaintext storage of authentication data
- Secure token refresh mechanism

### Hotkey Security
- Disable hotkeys when screen is locked
- Disable hotkeys when screensaver is active
- Use Windows API to detect lock/screensaver state

## Error Handling & Edge Cases

### Hotkey Conflicts
- **Detection**: Check for duplicate key assignments
- **User Feedback**: Warning dialog when conflicts detected
- **Behavior**: Prevent saving conflicting configurations

### Spotify Connection Issues
- **Missing Spotify**: No action taken (silent)
- **API Failures**: Continue operation, retry on next hotkey
- **Token Issues**: Show reconnection prompt in settings

### System Integration
- **Windows Lock**: Disable all hotkeys
- **Screensaver**: Disable all hotkeys
- **Application Focus**: Hotkeys work regardless of focused application

## Development Notes

### Required Dependencies
```xml
<dependencies>
    <dependency>
        <groupId>com.github.kwhat</groupId>
        <artifactId>jnativehook</artifactId>
        <version>2.2.2</version>
    </dependency>
    <dependency>
        <groupId>com.squareup.okhttp3</groupId>
        <artifactId>okhttp</artifactId>
        <version>4.12.0</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.15.2</version>
    </dependency>
</dependencies>
```

### Main Application Flow
1. **Startup**: Check for existing config, load settings
2. **System Tray**: Initialize tray icon and context menu
3. **Hotkey Registration**: Register global hotkeys with JNativeHook
4. **Spotify Auth**: Verify tokens or prompt for authentication
5. **Background Service**: Listen for hotkey events and execute API calls
6. **Shutdown**: Clean up resources and save configuration

### Key Classes Structure
- `ModulatifyApp` - Main application class
- `HotkeyManager` - Global hotkey handling
- `SpotifyController` - API integration
- `SettingsGUI` - Configuration window
- `SystemTrayManager` - Tray icon management
- `ConfigManager` - Settings persistence

This specification provides a complete blueprint for implementing Modulatify with all requested features and proper Windows integration.
