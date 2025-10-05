package com.prodexa.service;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

public class NotificationService {
    private TrayIcon trayIcon;

    public void initTrayIcon() {
        if (!SystemTray.isSupported()) {
            System.err.println("System tray not supported!");
            return;
        }

        // Load icon
        Image image = Toolkit.getDefaultToolkit()
                .getImage(getClass().getResource("/icons/app-icon.png"));

        PopupMenu popupMenu = new PopupMenu();

        // Add menu items
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        popupMenu.add(exitItem);

        trayIcon = new TrayIcon(image, "Prodexa Desktop - Status: Active", popupMenu);
        trayIcon.setImageAutoSize(true);

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public void updateStatus(String status) {
        if (trayIcon != null) {
            trayIcon.setToolTip("Prodexa Desktop - Status: " + status);
        }
    }
}
