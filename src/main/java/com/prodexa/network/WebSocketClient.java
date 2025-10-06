package com.prodexa.network;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

public class WebSocketClient implements WebSocket.Listener {
    private WebSocket webSocket;
    private final MessageHandler messageHandler;

    public WebSocketClient(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }


    public void connect(String serverUrl) {
        HttpClient client = HttpClient.newHttpClient();
        client.newWebSocketBuilder()
                .buildAsync(URI.create(serverUrl), this)
                .thenAccept(ws -> {
                    this.webSocket = ws;
                    System.out.println("✅ Connected to WebSocket server: " + serverUrl);
                });
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        System.out.println("WebSocket opened");
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        String message = data.toString();
        messageHandler.handleMessage(message);
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }
    public void sendMessage(String message) {
        if (webSocket != null) {
            webSocket.sendText(message, true);
            System.out.println("📤 Sent: " + message);
        } else {
            System.out.println("⚠️ WebSocket not connected yet.");
        }
    }

}