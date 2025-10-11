package com.prodexa.controller;

import com.prodexa.service.InputMonitoringService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML
    private Label keyLabel;

    @FXML
    private Label mouseLabel;

    @FXML
    private Button startButton;

    @FXML
    private Button stopButton;

    @FXML
    private Button breakButton;

    private InputMonitoringService monitoringService;
    private Thread updaterThread;

    private boolean isOnBreak = false;
    private boolean isMonitoring = false;

    public void initialize() {
        stopButton.setDisable(true);
        breakButton.setDisable(true);
    }

    @FXML
    private void handleStartButton() {
        if (monitoringService == null) {
            monitoringService = new InputMonitoringService();
        }

        monitoringService.start();
        isMonitoring = true;
        isOnBreak = false;

        startButton.setDisable(true);
        stopButton.setDisable(false);
        breakButton.setDisable(false);

        startUpdaterThread();
    }

    private void startUpdaterThread() {
        if (updaterThread == null || !updaterThread.isAlive()) {
            updaterThread = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        break;
                    }

                    if (isMonitoring && !isOnBreak) {
                        int keys = monitoringService.getKeyPressCount();
                        int clicks = monitoringService.getMouseClickCount();

                        Platform.runLater(() -> {
                            keyLabel.setText("Keys Pressed: " + keys);
                            mouseLabel.setText("Mouse Clicks: " + clicks);
                        });
                    }
                }
            });
            updaterThread.setDaemon(true);
            updaterThread.start();
        }
    }

    @FXML
    private void handleBreakButton() {
        if (isOnBreak) {
            isOnBreak = false;
            breakButton.setText("Take Break");
            monitoringService.resumeMonitoring();
        } else {
            isOnBreak = true;
            breakButton.setText("Resume Work");
            monitoringService.pauseMonitoring();
        }
    }

    @FXML
    private void handleStopButton() {
        if (monitoringService != null) {
            int totalKeys = monitoringService.getKeyPressCount();
            int totalClicks = monitoringService.getMouseClickCount();

            // Send to backend (replace with your backend call)
            System.out.println("Sending to backend -> Keys: " + totalKeys + ", Clicks: " + totalClicks);

            monitoringService.stop();
            monitoringService.resetCounts();
        }

        isMonitoring = false;
        startButton.setDisable(false);
        stopButton.setDisable(true);
        breakButton.setDisable(true);
        breakButton.setText("Take Break");

        Platform.runLater(() -> {
            keyLabel.setText("Keys Pressed: 0");
            mouseLabel.setText("Mouse Clicks: 0");
        });
    }
}

