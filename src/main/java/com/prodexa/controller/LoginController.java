package com.prodexa.controller;

import com.prodexa.network.KeycloakCallbackServer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;

import java.awt.Desktop;
import java.net.URI;
import java.io.IOException;

public class LoginController {

    @FXML
    private Label statusLabel;

    @FXML
    private Button loginButton;

    // Keycloak details
    private static final String KEYCLOAK_AUTH_URL = "http://localhost:8090/realms/Prodexa/protocol/openid-connect/auth";
    private static final String CLIENT_ID = "prodexa-javafx";
    private static final String REDIRECT_URI = "http://localhost:8088/dashboard";
    private static final String RESPONSE_TYPE = "code";
    private static final String SCOPE = "openid profile email";

    @FXML
    public void handleLogin() {
        try {
            // Step 1: Start small HTTP callback server to handle Keycloak redirect
            KeycloakCallbackServer.start(this::onLoginSuccess);

            // Step 2: Build Keycloak login URL
            String authUrl = KEYCLOAK_AUTH_URL
                    + "?client_id=" + CLIENT_ID
                    + "&redirect_uri=" + REDIRECT_URI
                    + "&response_type=" + RESPONSE_TYPE
                    + "&scope=" + SCOPE;

            // Step 3: Open Keycloak login page in browser
            Desktop.getDesktop().browse(new URI(authUrl));

            statusLabel.setText("Opening Keycloak login...");
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Callback from KeycloakCallbackServer when login succeeds
    private void onLoginSuccess(String accessToken) {
        System.out.println("✅ Login successful!");
        System.out.println("Access Token: " + accessToken);

        // Navigate to Dashboard
        javafx.application.Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/prodexa/dashboard.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) loginButton.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setFullScreen(true);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                statusLabel.setText("Failed to load dashboard");
            }
        });
    }
}
