package com.prodexa.controller;

import com.prodexa.service.InputMonitoringService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML
    private Label keyLabel;

    @FXML
    private Label mouseLabel;

    private InputMonitoringService monitoringService;

    public void initialize() {
        monitoringService = new InputMonitoringService();
        monitoringService.start();

        Thread updater = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int keys = monitoringService.getKeyPressCount();
                int clicks = monitoringService.getMouseClickCount();

                javafx.application.Platform.runLater(() -> {
                    keyLabel.setText("Keys Pressed: " + keys);
                    mouseLabel.setText("Mouse Clicks: " + clicks);
                });
            }
        });

        updater.setDaemon(true);
        updater.start();
    }
}