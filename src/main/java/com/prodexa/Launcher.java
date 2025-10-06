package com.prodexa;

import com.prodexa.network.MessageHandler;
import com.prodexa.network.WebSocketClient;
import com.prodexa.scheduler.HeartbeatScheduler;
import com.prodexa.service.HeartbeatService;
import com.prodexa.service.NotificationService;
import javafx.application.Application;
import javafx.stage.Stage;

public class Launcher extends Application {

    private NotificationService notificationService;
    private WebSocketClient webSocketClient;

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1️⃣ Initialize system tray
            notificationService = new NotificationService();
            notificationService.initTrayIcon();
            notificationService.updateStatus("Active");

            // 2️⃣ Initialize WebSocket connection
            MessageHandler messageHandler = new MessageHandler();
            webSocketClient = new WebSocketClient(messageHandler);

            // Temporary test WebSocket URL
            String serverUrl = "wss://echo.websocket.events";
            webSocketClient.connect(serverUrl);

            // 3️⃣ Initialize Heartbeat
            HeartbeatService heartbeatService = new HeartbeatService(primaryStage);
            HeartbeatScheduler heartbeatScheduler = new HeartbeatScheduler(heartbeatService, webSocketClient);
            heartbeatScheduler.start();

            // 4️⃣ Optional: send a test message once connected
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // wait for connection
                    webSocketClient.sendMessage("{\"type\": \"heartbeat\", \"status\": \"active\"}");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            // 5️⃣ Minimal JavaFX stage setup
            primaryStage.setTitle("Prodexa Desktop");
            primaryStage.setWidth(400);
            primaryStage.setHeight(200);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        System.out.println(" Application stopped.");
        if (notificationService != null) {
            notificationService.updateStatus("Offline");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
