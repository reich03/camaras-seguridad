package com.security.camera.desktop;

import com.google.gson.Gson;
import okhttp3.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Servicio para enviar frames en tiempo real al servidor mediante HTTP POST
 * Alternativa a WebSocket para simplificar la implementación
 */
public class StreamingService {
    
    private final String serverUrl;
    private final OkHttpClient client;
    private final Gson gson;
    private boolean streaming = false;

    public StreamingService(String serverUrl) {
        this.serverUrl = serverUrl;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    /**
     * Envía un frame al servidor para streaming
     */
    public void sendFrame(Long cameraId, String cameraName, BufferedImage frame) throws IOException {
        if (!streaming) return;

        // Convertir imagen a Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(frame, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // Crear payload JSON
        Map<String, Object> payload = new HashMap<>();
        payload.put("cameraId", cameraId);
        payload.put("cameraName", cameraName);
        payload.put("imageData", base64Image);
        payload.put("timestamp", System.currentTimeMillis());
        payload.put("grayscale", true);
        payload.put("scaleFactor", 0.5);
        payload.put("brightness", 0);
        payload.put("rotationAngle", 0);

        String jsonPayload = gson.toJson(payload);

        // Enviar mediante HTTP POST
        RequestBody body = RequestBody.create(
            jsonPayload, 
            MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(serverUrl + "/api/stream/frame")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Error sending frame: " + response.code());
            }
        } catch (Exception e) {
            System.err.println("Error sending frame: " + e.getMessage());
        }
    }

    public void startStreaming() {
        this.streaming = true;
    }

    public void stopStreaming() {
        this.streaming = false;
    }

    public boolean isStreaming() {
        return streaming;
    }
}
