# Modulatify - Windows Spotify Control App

A Java-based Windows background application that provides global hotkey control for Spotify playback functions.

## Features

- **Global Hotkeys**: Control Spotify from any application
  - Skip Forward: `Ctrl + Alt + O`
  - Skip Backward: `Ctrl + Alt + I`
  - Play/Pause: `Ctrl + Alt + P`
  - Volume Down: `Ctrl + Alt + K`
  - Volume Up: `Ctrl + Alt + L`

- **System Tray Integration**: Runs minimized to system tray
- **Settings GUI**: Configure hotkeys and Spotify connection
- **Spotify Web API**: Full integration with Spotify's Web API
- **Secure Token Storage**: Encrypted token storage using AES encryption

## Requirements

- Java 11 or higher
- Windows 10/11
- Spotify Premium account
- Maven (for building)

## Setup

### 1. Spotify App Registration

1. Go to [Spotify Developer Dashboard](https://developer.spotify.com/dashboard)
2. Create a new app
3. Note your `Client ID` and `Client Secret`
4. Add `http://localhost:8080/callback` as a redirect URI

### 2. Configuration

Edit `SpotifyController.java` and update:
```java
private static final String CLIENT_ID = "your_spotify_client_id";
private static final String CLIENT_SECRET = "your_spotify_client_secret";
```

### 3. Building

```bash
mvn clean package
```

### 4. Running

```bash
java -jar target/modulatify-1.0.0.jar
```

## Icon Setup

Place your system tray icon as:
- `src/main/resources/icons/modulatify_icon.png` (16x16 or 32x32 pixels)

If no icon is provided, a default green circle with "M" will be used.

## Configuration Storage

Settings are stored in: `%APPDATA%/Modulatify/config.properties`

## Architecture

- **ModulatifyApp**: Main application class and entry point
- **SystemTrayManager**: System tray integration and icon management
- **HotkeyManager**: Global hotkey capture using JNativeHook
- **SpotifyController**: Spotify Web API integration
- **ConfigManager**: Configuration persistence with encrypted token storage
- **SettingsGUI**: Settings window for configuration

## Dependencies

- JNativeHook 2.2.2 - Global hotkey capture
- OkHttp 4.12.0 - HTTP client for Spotify API
- Jackson 2.15.2 - JSON processing

## License

This project is for educational purposes. Make sure to comply with Spotify's API terms of service.# modulatify
