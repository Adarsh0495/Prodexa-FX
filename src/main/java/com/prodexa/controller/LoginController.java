package com.prodexa.controller;

import com.prodexa.model.LoginResponse;
import com.prodexa.service.AuthService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private CheckBox privacyPolicyCheck;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill all fields.");
            return;
        }
        if (!privacyPolicyCheck.isSelected()) {
            statusLabel.setText("Please agree to the Privacy Policy.");
            return;
        }

        new Thread(() -> {
            try {
                LoginResponse loginResponse = authService.login(email, password);

                Platform.runLater(() -> {
                    try {
                        statusLabel.setText("Login successful!");

                        com.example.prodexadesktop.service.UserSession.getInstance().setSession(
                                loginResponse.getEmail(),
                                loginResponse.getToken()
                        );

                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/prodexa/dashboard.fxml"));
                        Parent root = loader.load();

                        Stage stage = (Stage) emailField.getScene().getWindow();
                        Scene scene = new Scene(root);
                        stage.setScene(scene);
                        stage.setTitle("Dashboard");
                        stage.show();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        statusLabel.setText("Failed to load dashboard: " + ex.getMessage());
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Forgot Password");
        alert.setHeaderText("Password Recovery");
        alert.setContentText("Please contact your mentor or admin to reset your password.");
        alert.showAndWait();
    }
}
