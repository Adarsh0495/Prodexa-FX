package com.prodexa.service;

import java.util.Map;

public class SessionManager {

    private static SessionManager instance;

    private String accessToken;
    private String refreshToken;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }


    public void createSession(Map<String, String> tokens) {
        this.accessToken = tokens.get("access_token");
        this.refreshToken = tokens.get("refresh_token");
        System.out.println("Session created successfully.");
    }

    public void logout() {
        this.accessToken = null;
        this.refreshToken = null;
        System.out.println("Session cleared.");
    }

    public String getAccessToken() {
        return accessToken;
    }

    public boolean isAuthenticated() {
        return accessToken != null && !accessToken.isBlank();
    }
}