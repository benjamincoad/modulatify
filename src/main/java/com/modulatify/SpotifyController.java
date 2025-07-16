package com.modulatify;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SpotifyController {
    private static final Logger logger = Logger.getLogger(SpotifyController.class.getName());
    
    private static final String CLIENT_ID = "your_spotify_client_id";
    private static final String CLIENT_SECRET = "your_spotify_client_secret";
    private static final String REDIRECT_URI = "http://localhost:8080/callback";
    private static final String SPOTIFY_API_BASE = "https://api.spotify.com/v1";
    private static final String SPOTIFY_ACCOUNTS_BASE = "https://accounts.spotify.com";
    
    private final ConfigManager configManager;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private int currentVolume = 50;
    
    public SpotifyController(ConfigManager configManager) {
        this.configManager = configManager;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    public boolean isAuthenticated() {
        String accessToken = configManager.getSpotifyAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            return false;
        }
        
        long expiresAt = configManager.getTokenExpiresAt();
        return System.currentTimeMillis() < expiresAt;
    }
    
    public String getAuthorizationUrl() {
        String scope = "user-read-playback-state user-modify-playback-state";
        try {
            return SPOTIFY_ACCOUNTS_BASE + "/authorize?" +
                    "client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8) +
                    "&response_type=code" +
                    "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
                    "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.severe("Failed to create authorization URL: " + e.getMessage());
            return "";
        }
    }
    
    public boolean exchangeCodeForTokens(String code) {
        try {
            String credentials = Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes());
            
            RequestBody formBody = new FormBody.Builder()
                    .add("grant_type", "authorization_code")
                    .add("code", code)
                    .add("redirect_uri", REDIRECT_URI)
                    .build();
            
            Request request = new Request.Builder()
                    .url(SPOTIFY_ACCOUNTS_BASE + "/api/token")
                    .header("Authorization", "Basic " + credentials)
                    .post(formBody)
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonNode jsonResponse = objectMapper.readTree(response.body().string());
                    
                    String accessToken = jsonResponse.get("access_token").asText();
                    String refreshToken = jsonResponse.get("refresh_token").asText();
                    int expiresIn = jsonResponse.get("expires_in").asInt();
                    
                    configManager.setSpotifyAccessToken(accessToken);
                    configManager.setSpotifyRefreshToken(refreshToken);
                    configManager.setTokenExpiresAt(System.currentTimeMillis() + (expiresIn * 1000L));
                    
                    logger.info("Successfully obtained Spotify tokens");
                    return true;
                } else {
                    logger.warning("Failed to exchange code for tokens: " + response.code());
                    return false;
                }
            }
        } catch (Exception e) {
            logger.severe("Error exchanging code for tokens: " + e.getMessage());
            return false;
        }
    }
    
    public boolean refreshAccessToken() {
        try {
            String refreshToken = configManager.getSpotifyRefreshToken();
            if (refreshToken == null || refreshToken.isEmpty()) {
                return false;
            }
            
            String credentials = Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes());
            
            RequestBody formBody = new FormBody.Builder()
                    .add("grant_type", "refresh_token")
                    .add("refresh_token", refreshToken)
                    .build();
            
            Request request = new Request.Builder()
                    .url(SPOTIFY_ACCOUNTS_BASE + "/api/token")
                    .header("Authorization", "Basic " + credentials)
                    .post(formBody)
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonNode jsonResponse = objectMapper.readTree(response.body().string());
                    
                    String accessToken = jsonResponse.get("access_token").asText();
                    int expiresIn = jsonResponse.get("expires_in").asInt();
                    
                    configManager.setSpotifyAccessToken(accessToken);
                    configManager.setTokenExpiresAt(System.currentTimeMillis() + (expiresIn * 1000L));
                    
                    logger.info("Successfully refreshed Spotify access token");
                    return true;
                } else {
                    logger.warning("Failed to refresh access token: " + response.code());
                    return false;
                }
            }
        } catch (Exception e) {
            logger.severe("Error refreshing access token: " + e.getMessage());
            return false;
        }
    }
    
    private boolean ensureValidToken() {
        if (!isAuthenticated()) {
            return refreshAccessToken();
        }
        return true;
    }
    
    public void skipForward() {
        if (!ensureValidToken()) {
            logger.warning("No valid token for skip forward");
            return;
        }
        
        makeSpotifyRequest("POST", "/me/player/next", null);
    }
    
    public void skipBackward() {
        if (!ensureValidToken()) {
            logger.warning("No valid token for skip backward");
            return;
        }
        
        makeSpotifyRequest("POST", "/me/player/previous", null);
    }
    
    public void togglePlayPause() {
        if (!ensureValidToken()) {
            logger.warning("No valid token for play/pause");
            return;
        }
        
        try {
            JsonNode playerState = getPlayerState();
            if (playerState != null && playerState.has("is_playing")) {
                boolean isPlaying = playerState.get("is_playing").asBoolean();
                if (isPlaying) {
                    makeSpotifyRequest("PUT", "/me/player/pause", null);
                } else {
                    makeSpotifyRequest("PUT", "/me/player/play", null);
                }
            }
        } catch (Exception e) {
            logger.warning("Error toggling play/pause: " + e.getMessage());
        }
    }
    
    public void volumeUp() {
        if (!ensureValidToken()) {
            logger.warning("No valid token for volume up");
            return;
        }
        
        currentVolume = Math.min(100, currentVolume + 10);
        setVolume(currentVolume);
    }
    
    public void volumeDown() {
        if (!ensureValidToken()) {
            logger.warning("No valid token for volume down");
            return;
        }
        
        currentVolume = Math.max(0, currentVolume - 10);
        setVolume(currentVolume);
    }
    
    private void setVolume(int volume) {
        makeSpotifyRequest("PUT", "/me/player/volume?volume_percent=" + volume, null);
    }
    
    private JsonNode getPlayerState() throws IOException {
        String accessToken = configManager.getSpotifyAccessToken();
        
        Request request = new Request.Builder()
                .url(SPOTIFY_API_BASE + "/me/player")
                .header("Authorization", "Bearer " + accessToken)
                .get()
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return objectMapper.readTree(response.body().string());
            }
        }
        return null;
    }
    
    private void makeSpotifyRequest(String method, String endpoint, RequestBody body) {
        try {
            String accessToken = configManager.getSpotifyAccessToken();
            
            Request.Builder requestBuilder = new Request.Builder()
                    .url(SPOTIFY_API_BASE + endpoint)
                    .header("Authorization", "Bearer " + accessToken);
            
            switch (method) {
                case "GET":
                    requestBuilder.get();
                    break;
                case "POST":
                    requestBuilder.post(body != null ? body : RequestBody.create("", null));
                    break;
                case "PUT":
                    requestBuilder.put(body != null ? body : RequestBody.create("", null));
                    break;
                default:
                    logger.warning("Unsupported HTTP method: " + method);
                    return;
            }
            
            try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
                if (!response.isSuccessful()) {
                    logger.warning("Spotify API request failed: " + response.code() + " for " + endpoint);
                }
            }
        } catch (Exception e) {
            logger.warning("Error making Spotify API request: " + e.getMessage());
        }
    }
}