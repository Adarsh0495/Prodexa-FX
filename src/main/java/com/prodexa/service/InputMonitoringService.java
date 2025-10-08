package com.prodexa.service;


import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;

import java.util.logging.Level;
import java.util.logging.Logger;

public class InputMonitoringService implements NativeKeyListener, NativeMouseListener {

    private int keyPressCount = 0;
    private int mouseClickCount = 0;

    public void start() {
        try {
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.WARNING);

            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
            GlobalScreen.addNativeMouseListener(this);
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        keyPressCount++;
        System.out.println("Key pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
        System.out.println("Total Keys Pressed: " + keyPressCount);
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
    }


    @Override
    public void nativeMouseClicked(NativeMouseEvent e) {
        mouseClickCount++;
        System.out.println("Mouse clicked at: " + e.getX() + ", " + e.getY());
        System.out.println("Total Mouse Clicks: " + mouseClickCount);
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent e) {
    }

    public int getKeyPressCount() {
        return keyPressCount;
    }

    public int getMouseClickCount() {
        return mouseClickCount;
    }
}



