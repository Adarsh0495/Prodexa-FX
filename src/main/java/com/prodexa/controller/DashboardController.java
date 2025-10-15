package com.prodexa.controller;

import com.prodexa.service.CaptureService;
import com.prodexa.service.InputMonitoringService;
import com.prodexa.service.SessionManager;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Map;

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
    private final CaptureService captureService = new CaptureService();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private Thread updaterThread;

    private boolean isOnBreak = false;
    private boolean isMonitoring = false;

    public void initialize() {
//        sessionManager.createSession(Map.of("access_token", "fake-test-token"));
//        startMockUploadServer();
        stopButton.setDisable(true);
        breakButton.setDisable(true);
    }

    @FXML
    private void handleStartButton() {
        if (monitoringService == null) {
            monitoringService = new InputMonitoringService();
        }

        monitoringService.start();
        captureService.startMonitoring();
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

            System.out.println("Sending to backend -> Keys: " + totalKeys + ", Clicks: " + totalClicks);

            monitoringService.stop();
            monitoringService.resetCounts();
        }
        captureService.stopMonitoring();

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

//    @FXML
//    private void handleTestScreenshot() {
//        System.out.println("--- UI TEST: Manually triggering screenshot ---");
//        captureService.captureAndUploadScreenshot();
//    }
//
//    @FXML
//    private void handleTestWebcam() {
//        System.out.println("--- UI TEST: Manually triggering webcam photo ---");
//        captureService.captureAndUploadWebcamPhoto();
//    }

//    private void startMockUploadServer() {
//        try {
//            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);
//            server.createContext("/api/captures/upload", exchange -> {
//                System.out.println("--- MOCK SERVER: RECEIVED UPLOAD ---");
//                exchange.getRequestHeaders().forEach((key, value) -> System.out.println("Header: " + key + " = " + value));
//
//                try (InputStream is = exchange.getRequestBody()) {
//                    byte[] data = is.readNBytes(100);
//                    System.out.println("Received " + is.available() + " more bytes of image data...");
//                }
//
//                String response = "{\"status\": \"mock upload successful\"}";
//                exchange.sendResponseHeaders(200, response.length());
//                exchange.getResponseBody().write(response.getBytes());
//                exchange.close();
//                System.out.println("--- MOCK SERVER: RESPONSE SENT ---");
//            });
//            server.setExecutor(null);
//            server.start();
//            System.out.println("Mock upload server started on port 8080.");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}

