module com.prodexa {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires java.net.http;
    requires com.google.gson;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.github.kwhat.jnativehook;

    opens com.prodexa to javafx.fxml, com.fasterxml.jackson.databind;
    exports com.prodexa;
    exports com.prodexa.controller;
    opens com.prodexa.controller to com.fasterxml.jackson.databind, javafx.fxml;
}