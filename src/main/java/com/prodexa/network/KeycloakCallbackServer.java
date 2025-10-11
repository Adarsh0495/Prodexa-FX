package com.prodexa.network;

import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.util.function.Consumer;

public class KeycloakCallbackServer {

    private static final String TOKEN_URL = "http://localhost:8080/realms/prodexa/protocol/openid-connect/token";
    private static final String CLIENT_ID = "prodexa-javafx";
    private static final String REDIRECT_URI = "http://localhost:8088/dashboard";

    public static void start(Consumer<String> onSuccess) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

        server.createContext("/callback", exchange -> {
            try {
                String query = exchange.getRequestURI().getQuery();
                String code = query.split("code=")[1];

                // Exchange code for token
                String form = "grant_type=authorization_code"
                        + "&code=" + code
                        + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8")
                        + "&client_id=" + CLIENT_ID;

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(TOKEN_URL))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(form))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                String tokenResponse = response.body();

                // Send success message to browser
                String html = "<html><body><h2>Login successful! You can close this tab.</h2></body></html>";
                exchange.sendResponseHeaders(200, html.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(html.getBytes());
                }

                System.out.println("🔐 Token Response: " + tokenResponse);
                String accessToken = extractAccessToken(tokenResponse);

                // Notify the JavaFX app
                onSuccess.accept(accessToken);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                server.stop(1);
            }
        });

        server.start();
        System.out.println("Callback server started on http://localhost:8081/callback");
    }

    private static String extractAccessToken(String json) {
        int start = json.indexOf("\"access_token\":\"") + 16;
        int end = json.indexOf("\"", start);
        if (start > 15 && end > start) {
            return json.substring(start, end);
        }
        return null;
    }
}
