package com.prodexa.service;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;

public class InputMonitoringService implements NativeKeyListener, NativeMouseListener {

    private int keyPressCount = 0;
    private int mouseClickCount = 0;
    private boolean isMonitoringActive = false;
    private boolean isPaused = false;

    public void start() {
        try {
            if (!isMonitoringActive) {
                if (!GlobalScreen.isNativeHookRegistered()) {
                    GlobalScreen.registerNativeHook();
                }
                GlobalScreen.addNativeKeyListener(this);
                GlobalScreen.addNativeMouseListener(this);
                isMonitoringActive = true;
                isPaused = false;
            }
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    public void pauseMonitoring() {
        isPaused = true;
    }

    public void resumeMonitoring() {
        isPaused = false;
    }

    public void stop() {
        if (isMonitoringActive) {
            try {
                GlobalScreen.removeNativeKeyListener(this);
                GlobalScreen.removeNativeMouseListener(this);
                isMonitoringActive = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void resetCounts() {
        keyPressCount = 0;
        mouseClickCount = 0;
    }

    public int getKeyPressCount() {
        return keyPressCount;
    }

    public int getMouseClickCount() {
        return mouseClickCount;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
        if (isMonitoringActive && !isPaused) keyPressCount++;
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent nativeEvent) {
        if (isMonitoringActive && !isPaused) mouseClickCount++;
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeEvent) {}
    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeEvent) {}
    @Override
    public void nativeMousePressed(NativeMouseEvent nativeEvent) {}
    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeEvent) {}
}
