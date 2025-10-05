package com.prodexa.network;

import com.prodexa.util.JsonUtil;

public class MessageHandler {

    public void handleMessage(String jsonMessage) {
        System.out.println("📩 Incoming: " + jsonMessage);

        if (jsonMessage.contains("heartbeat")) {
            System.out.println("✅ Heartbeat received");
        } else if (jsonMessage.contains("command")) {
            System.out.println("⚡ Command received");
        } else {
            System.out.println("ℹ️ Other message: " + jsonMessage);
        }
    }
}
