//package com.prodexa.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.prodexa.model.LoginResponse;
//
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//
//public class AuthService {
//    private static final String LOGIN_URL = "http://localhost:8080/auth/login";
//    private final HttpClient client;
//    private final ObjectMapper mapper;
//
//    public AuthService() {
//        this.client = HttpClient.newHttpClient();
//        this.mapper = new ObjectMapper();
//    }
//
//    public LoginResponse login(String email, String password) throws Exception {
//        String body = String.format("{\"email\":\"%s\", \"password\":\"%s\"}", email, password);
//
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(LOGIN_URL))
//                .header("Content-Type", "application/json")
//                .POST(HttpRequest.BodyPublishers.ofString(body))
//                .build();
//
//        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//        if (response.statusCode() == 200){
//            return mapper.readValue(response.body(), LoginResponse.class);
//        } else {
//            throw new Exception("Invalid credentials");
//        }
//    }
//}
