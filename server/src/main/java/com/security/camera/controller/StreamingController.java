package com.security.camera.controller;

import com.security.camera.service.ImageProcessingService;
import com.security.camera.service.ImageProcessingService.ImageFilters;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controlador para streaming de video en tiempo real
 */
@Controller
@RestController
@RequestMapping("/api/stream")
@CrossOrigin
@RequiredArgsConstructor
public class StreamingController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ImageProcessingService imageProcessingService;
    
    // Almacenar el último frame de cada cámara
    private final Map<Long, String> latestFrames = new ConcurrentHashMap<>();

    /**
     * Recibe frames desde el desktop client y los transmite a los web clients
     */
    @MessageMapping("/stream/frame")
    @SendTo("/topic/frames")
    public FrameMessage handleFrame(FrameMessage frameMessage) {
        try {
            // Decodificar imagen
            byte[] imageBytes = Base64.getDecoder().decode(frameMessage.getImageData());
            
            // Aplicar filtros configurados
            ImageFilters filters = new ImageFilters(
                frameMessage.isGrayscale(),
                frameMessage.getScaleFactor(),
                frameMessage.getBrightness(),
                frameMessage.getRotationAngle()
            );
            
            byte[] processedImage = imageProcessingService.processImage(imageBytes, filters);
            String processedBase64 = Base64.getEncoder().encodeToString(processedImage);
            
            // Actualizar último frame
            latestFrames.put(frameMessage.getCameraId(), processedBase64);
            
            // Crear mensaje de respuesta
            FrameMessage response = new FrameMessage();
            response.setCameraId(frameMessage.getCameraId());
            response.setCameraName(frameMessage.getCameraName());
            response.setImageData(processedBase64);
            response.setTimestamp(System.currentTimeMillis());
            
            return response;
        } catch (Exception e) {
            System.err.println("Error processing frame: " + e.getMessage());
            return frameMessage;
        }
    }

    /**
     * Endpoint REST para recibir frames por HTTP POST (alternativa a WebSocket)
     */
    @PostMapping("/frame")
    @ResponseBody
    public Map<String, Object> receiveFrame(@RequestBody FrameMessage frameMessage) {
        try {
            // Decodificar imagen
            byte[] imageBytes = Base64.getDecoder().decode(frameMessage.getImageData());
            
            // Aplicar filtros configurados
            ImageFilters filters = new ImageFilters(
                frameMessage.isGrayscale(),
                frameMessage.getScaleFactor(),
                frameMessage.getBrightness(),
                frameMessage.getRotationAngle()
            );
            
            byte[] processedImage = imageProcessingService.processImage(imageBytes, filters);
            String processedBase64 = Base64.getEncoder().encodeToString(processedImage);
            
            // Actualizar último frame
            latestFrames.put(frameMessage.getCameraId(), processedBase64);
            
            // Enviar a los clientes WebSocket suscritos
            FrameMessage wsMessage = new FrameMessage();
            wsMessage.setCameraId(frameMessage.getCameraId());
            wsMessage.setCameraName(frameMessage.getCameraName());
            wsMessage.setImageData(processedBase64);
            wsMessage.setTimestamp(System.currentTimeMillis());
            
            messagingTemplate.convertAndSend("/topic/frames", wsMessage);
            
            return Map.of("status", "success", "cameraId", frameMessage.getCameraId());
        } catch (Exception e) {
            System.err.println("Error processing frame: " + e.getMessage());
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    /**
     * Endpoint REST para obtener el último frame de una cámara
     */
    @GetMapping("/latest/{cameraId}")
    @ResponseBody
    public Map<String, Object> getLatestFrame(@PathVariable Long cameraId) {
        String frame = latestFrames.get(cameraId);
        return Map.of(
            "cameraId", cameraId,
            "imageData", frame != null ? frame : "",
            "available", frame != null
        );
    }

    /**
     * Endpoint REST para obtener todas las cámaras activas con streaming
     */
    @GetMapping("/active")
    @ResponseBody
    public Map<String, Object> getActiveCameras() {
        return Map.of(
            "activeCameras", latestFrames.keySet(),
            "count", latestFrames.size()
        );
    }

    /**
     * DTO para mensajes de frames
     */
    public static class FrameMessage {
        private Long cameraId;
        private String cameraName;
        private String imageData; // Base64
        private long timestamp;
        private boolean grayscale = true;
        private double scaleFactor = 0.5;
        private int brightness = 0;
        private int rotationAngle = 0;

        // Getters y Setters
        public Long getCameraId() { return cameraId; }
        public void setCameraId(Long cameraId) { this.cameraId = cameraId; }

        public String getCameraName() { return cameraName; }
        public void setCameraName(String cameraName) { this.cameraName = cameraName; }

        public String getImageData() { return imageData; }
        public void setImageData(String imageData) { this.imageData = imageData; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        public boolean isGrayscale() { return grayscale; }
        public void setGrayscale(boolean grayscale) { this.grayscale = grayscale; }

        public double getScaleFactor() { return scaleFactor; }
        public void setScaleFactor(double scaleFactor) { this.scaleFactor = scaleFactor; }

        public int getBrightness() { return brightness; }
        public void setBrightness(int brightness) { this.brightness = brightness; }

        public int getRotationAngle() { return rotationAngle; }
        public void setRotationAngle(int rotationAngle) { this.rotationAngle = rotationAngle; }
    }
}
