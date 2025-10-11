//package com.example.prodexadesktop.service;
//
//public class UserSession {
//    private static UserSession instance;
//    private String email;
//    private String token;
//
//    private UserSession() {}
//
//    public static UserSession getInstance() {
//        if (instance == null) {
//            instance = new UserSession();
//        }
//        return instance;
//    }
//
//    public void setSession(String email, String token) {
//        this.email = email;
//        this.token = token;
//    }
//
//    public String getEmail() { return email; }
//    public String getToken() { return token; }
//
//    public void clear() {
//        email = null;
//        token = null;
//    }
//}
