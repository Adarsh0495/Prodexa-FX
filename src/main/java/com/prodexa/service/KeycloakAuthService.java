package com.prodexa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class KeycloakAuthService {

    private static final String KEYCLOAK_HOST = "http://localhost:8090";
    private static final String REALM = "Prodexa";
    private static final String CLIENT_ID = "prodexa-javafx";
    private static final int CALLBACK_PORT = 8088;
    private static final String REDIRECT_URI = "http://localhost:" + CALLBACK_PORT + "/dashboard";

    private static final String AUTH_URL = KEYCLOAK_HOST + "/realms/" + REALM + "/protocol/openid-connect/auth";
    private static final String TOKEN_URL = KEYCLOAK_HOST + "/realms/" + REALM + "/protocol/openid-connect/token";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String codeVerifier;


    public CompletableFuture<Map<String, String>> authenticate() {
        CompletableFuture<Map<String, String>> authFuture = new CompletableFuture<>();
        try {
            this.codeVerifier = generateCodeVerifier();
            String codeChallenge = generateCodeChallenge(codeVerifier);
            String scope = "openid profile email";
            String authUrl = AUTH_URL +
                    "?client_id=" + CLIENT_ID +
                    "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
                    "&response_type=code" +
                    "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8) +
                    "&code_challenge=" + codeChallenge +
                    "&code_challenge_method=S256";

            startInternalCallbackServer(authFuture);

            Desktop.getDesktop().browse(new URI(authUrl));
        } catch (Exception e) {
            e.printStackTrace();
            authFuture.completeExceptionally(e);
        }
        return authFuture;
    }

    private void startInternalCallbackServer(CompletableFuture<Map<String, String>> authFuture) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(CALLBACK_PORT), 0);
        server.createContext("/dashboard", httpExchange -> {
            String query = httpExchange.getRequestURI().getQuery();
            String authCode = null;
            if (query != null && query.contains("code=")) {
                for (String param : query.split("&")) {
                    if (param.startsWith("code=")) {
                        authCode = param.substring(5);
                        break;
                    }
                }
            }

            String response = "<h1>Authentication Successful!</h1><p>You can close this window now.</p>";
            httpExchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = httpExchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            server.stop(1);

            if (authCode != null) {
                exchangeCodeForTokens(authCode, authFuture);
            } else {
                authFuture.completeExceptionally(new RuntimeException("Authentication failed: No authorization code received."));
            }
        });
        server.setExecutor(null);
        server.start();
    }

    private void exchangeCodeForTokens(String authCode, CompletableFuture<Map<String, String>> authFuture) {
        try {
            String body = "grant_type=authorization_code" +
                    "&code=" + authCode +
                    "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
                    "&client_id=" + CLIENT_ID +
                    "&code_verifier=" + this.codeVerifier;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TOKEN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, String> tokens = objectMapper.readValue(response.body(), Map.class);
                authFuture.complete(tokens);
            } else {
                authFuture.completeExceptionally(new RuntimeException("Failed to retrieve tokens. Server response: " + response.body()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            authFuture.completeExceptionally(e);
        }
    }

    private String generateCodeVerifier() {
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String generateCodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }
}