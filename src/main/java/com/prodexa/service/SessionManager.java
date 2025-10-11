package com.prodexa.service;

import java.util.Map;

/**
 * A Singleton class to manage the user's session state.
 * It holds the authentication tokens and provides a global access point to them.
 */
public class SessionManager {

    // The single, static instance of this class
    private static SessionManager instance;

    private String accessToken;
    private String refreshToken;
    // You could also add user details like name, email, roles, etc.

    // Private constructor prevents anyone else from creating an instance
    private SessionManager() {}

    /**
     * Provides the global access point to the SessionManager instance.
     * This is the only way to get a reference to the session manager.
     * @return The single instance of SessionManager.
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Initializes the user session with the tokens received from Keycloak.
     * @param tokens A map containing "access_token", "refresh_token", etc.
     */
    public void createSession(Map<String, String> tokens) {
        this.accessToken = tokens.get("access_token");
        this.refreshToken = tokens.get("refresh_token");

        // In a real-world app, you would securely store the refresh token
        // using a library like 'java-keyring' that interfaces with the OS keychain.
        System.out.println("Session created successfully.");
    }

    /**
     * Clears all session data, effectively logging the user out.
     */
    public void logout() {
        this.accessToken = null;
        this.refreshToken = null;
        // Also clear any securely stored tokens.
        System.out.println("Session cleared.");
    }

    /**
     * Retrieves the current access token.
     * @return The access token, or null if the user is not authenticated.
     */
    public String getAccessToken() {
        // FUTURE IMPROVEMENT: Add logic here to check if the token is expired.
        // If it is, use the refreshToken to silently get a new accessToken
        // from Keycloak's token endpoint before returning it.
        return accessToken;
    }

    /**
     * Checks if there is an active session.
     * @return true if the user is authenticated, false otherwise.
     */
    public boolean isAuthenticated() {
        return accessToken != null && !accessToken.isBlank();
    }
}