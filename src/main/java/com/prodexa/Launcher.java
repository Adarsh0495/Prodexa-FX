package com.prodexa;

import com.prodexa.service.NotificationService;
import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        Application.launch(HelloApplication.class, args);
        NotificationService notificationService = new NotificationService();
        notificationService.initTrayIcon();
        notificationService.updateStatus("Active");
    }
}
