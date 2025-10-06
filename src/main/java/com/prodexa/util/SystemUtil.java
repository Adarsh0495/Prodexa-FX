package com.prodexa.util;

import javafx.stage.Stage;

public class SystemUtil {

    public static String getWindowState(Stage stage) {
        if (stage == null) return "unknown";
        if (!stage.isShowing()) return "minimized";
        if (!stage.isFocused()) return "idle";
        return "active";
    }

    public static String getUserId() {

        return "user-001";
    }
}
