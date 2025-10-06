package com.prodexa.scheduler;

import com.prodexa.network.WebSocketClient;
import com.prodexa.service.HeartbeatService;

import java.util.Timer;
import java.util.TimerTask;

public class HeartbeatScheduler {

    private final Timer timer = new Timer(true);
    private final HeartbeatService heartbeatService;
    private final WebSocketClient webSocketClient;

    // mock counters from InputActivityService (Member 1)
    private int mouseMoves = 0;
    private int keyPresses = 0;
    private long lastActive = System.currentTimeMillis();

    public HeartbeatScheduler(HeartbeatService heartbeatService, WebSocketClient webSocketClient) {
        this.heartbeatService = heartbeatService;
        this.webSocketClient = webSocketClient;
    }

    public void start() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String heartbeat = heartbeatService.createHeartbeatPayload(
                        mouseMoves, keyPresses, lastActive
                );
                System.out.println("💓 Sending heartbeat: " + heartbeat);
                webSocketClient.sendMessage(heartbeat);
            }
        }, 0, 30_000); // every 30s
    }

    public void stop() {
        timer.cancel();
    }
}
