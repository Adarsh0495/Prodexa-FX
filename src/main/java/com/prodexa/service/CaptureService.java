package com.prodexa.service;

import com.github.sarxos.webcam.Webcam;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.*;

public class CaptureService {

    private static final String BACKEND_UPLOAD_URL = "http://localhost:8080/api/captures/upload";
    private static final String BACKEND_WEBSOCKET_URL = "ws://localhost:8080/ws/capture-commands";
    private static final long AUTOMATIC_CAPTURE_INTERVAL_MINUTES = 15;

    private final SessionManager sessionManager = SessionManager.getInstance();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private ScheduledExecutorService scheduler;
    private WebSocket webSocket;

    public void startMonitoring() {
        startAutomaticScreenshots();
        connectToWebSocket();
    }

    public void stopMonitoring() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Session ended").join();
        }
    }

    private void startAutomaticScreenshots() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::captureAndUploadScreenshot,
                AUTOMATIC_CAPTURE_INTERVAL_MINUTES,
                AUTOMATIC_CAPTURE_INTERVAL_MINUTES,
                TimeUnit.MINUTES);
        System.out.println("Automatic screenshot capture scheduled every " + AUTOMATIC_CAPTURE_INTERVAL_MINUTES + " minutes.");
    }

    private void connectToWebSocket() {
        if (!sessionManager.isAuthenticated()) {
            System.err.println("Cannot connect to WebSocket. User not authenticated.");
            return;
        }

        System.out.println("Connecting to WebSocket at " + BACKEND_WEBSOCKET_URL);
        httpClient.newWebSocketBuilder()
                .header("Authorization", "Bearer " + sessionManager.getAccessToken())
                .buildAsync(URI.create(BACKEND_WEBSOCKET_URL), new WebSocketClientListener())
                .thenAccept(ws -> this.webSocket = ws)
                .join();
    }

    private class WebSocketClientListener implements WebSocket.Listener {
        @Override
        public void onOpen(WebSocket ws) {
            System.out.println("WebSocket connection opened.");
            ws.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
            String command = data.toString();
            System.out.println("Received command from server: " + command);
            if ("TAKE_SCREENSHOT".equalsIgnoreCase(command)) {
                captureAndUploadScreenshot();
            } else if ("TAKE_WEBCAM_PHOTO".equalsIgnoreCase(command)) {
                captureAndUploadWebcamPhoto();
            }
            ws.request(1);
            return null;
        }

        @Override
        public void onError(WebSocket ws, Throwable error) {
            System.err.println("WebSocket error: " + error.getMessage());
            error.printStackTrace();
        }
    }

    public void captureAndUploadScreenshot() {
        System.out.println("Capturing screenshot...");
        try {
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenshot = new Robot().createScreenCapture(screenRect);
            byte[] compressedImage = compressImage(screenshot, 0.5f); // 50% quality
            uploadImage(compressedImage, "screenshot.jpg");
        } catch (AWTException | IOException e) {
            e.printStackTrace();
        }
    }

    public void captureAndUploadWebcamPhoto() {
        System.out.println("Capturing webcam photo...");
        Webcam webcam = Webcam.getDefault();
        if (webcam == null) {
            System.err.println("No webcam found.");
            return;
        }
        try {
            webcam.open();
            BufferedImage webcamImage = webcam.getImage();
            byte[] compressedImage = compressImage(webcamImage, 0.7f); // 70% quality
            uploadImage(compressedImage, "webcam.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            webcam.close();
        }
    }

    private byte[] compressImage(BufferedImage image, float quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
        jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpgWriteParam.setCompressionQuality(quality);

        try (ImageOutputStream outputStream = ImageIO.createImageOutputStream(baos)) {
            jpgWriter.setOutput(outputStream);
            jpgWriter.write(null, new javax.imageio.IIOImage(image, null, null), jpgWriteParam);
            jpgWriter.dispose();
        }

        System.out.println("Original size approx: " + (image.getWidth() * image.getHeight() * 3) + " bytes. Compressed size: " + baos.size() + " bytes.");
        return baos.toByteArray();
    }

    private void uploadImage(byte[] imageData, String fileName) {
        if (!sessionManager.isAuthenticated()) {
            System.err.println("Cannot upload image. User not authenticated.");
            return;
        }

        try {
            String boundary = UUID.randomUUID().toString();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BACKEND_UPLOAD_URL))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .header("Authorization", "Bearer " + sessionManager.getAccessToken())
                    .timeout(Duration.ofMinutes(1))
                    .POST(ofMultipartData(imageData, fileName, boundary))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(System.out::println)
                    .exceptionally(e -> {
                        System.err.println("Upload failed: " + e.getMessage());
                        return null;
                    });
            System.out.println("Uploading " + fileName + " to backend...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HttpRequest.BodyPublisher ofMultipartData(byte[] data, String fileName, String boundary) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String EOL = "\r\n";
        baos.write(("--" + boundary + EOL).getBytes());
        baos.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + EOL).getBytes());
        baos.write(("Content-Type: image/jpeg" + EOL + EOL).getBytes());
        baos.write(data);
        baos.write((EOL + "--" + boundary + "--" + EOL).getBytes());
        return HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray());
    }
}