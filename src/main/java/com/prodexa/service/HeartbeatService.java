package com.prodexa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prodexa.util.SystemUtil;
import javafx.stage.Stage;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class HeartbeatService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Stage stage;

    public HeartbeatService(Stage stage) {
        this.stage = stage;
    }

    public String createHeartbeatPayload(int mouseCount, int keyCount, long lastActiveTime) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", SystemUtil.getUserId());
            payload.put("timestamp", Instant.now().toString());
            payload.put("status", SystemUtil.getWindowState(stage));
            payload.put("mouseMoves", mouseCount);
            payload.put("keyPresses", keyCount);
            payload.put("lastActive", lastActiveTime);
            return mapper.writeValueAsString(payload);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }
}
