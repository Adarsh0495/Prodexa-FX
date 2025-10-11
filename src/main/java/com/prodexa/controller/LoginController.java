
package com.prodexa.controller;

import com.prodexa.service.KeycloakAuthService;
import com.prodexa.service.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private Label statusLabel;
    @FXML
    private Button loginButton;

    private final KeycloakAuthService authService = new KeycloakAuthService();
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    public void handleLogin() {
        loginButton.setDisable(true);
        statusLabel.setText("Opening Keycloak login...");

        authService.authenticate().whenComplete((tokens, error) -> {
            Platform.runLater(() -> {
                if (tokens != null) {
                    System.out.println("Login successful!");
                    sessionManager.createSession(tokens);
                    navigateToDashboard();
                } else {
                    System.err.println("Login failed: " + error.getMessage());
                    statusLabel.setText("Error: Login failed.");
                    loginButton.setDisable(false);
                }
            });
        });
    }

    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/prodexa/dashboard.fxml")); // Assuming it's main-view.fxml now
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Prodexa Dashboard");
            stage.setFullScreen(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Failed to load dashboard");
        }
    }
}