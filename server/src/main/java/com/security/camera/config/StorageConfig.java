package com.security.camera.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.io.File;

/**
 * Configuración de almacenamiento para videos y frames
 */
@Configuration
public class StorageConfig {

    @Value("${storage.video.path:/app/videos}")
    private String videoStoragePath;

    @Value("${storage.frame.path:/app/frames}")
    private String frameStoragePath;

    @PostConstruct
    public void init() {
        createDirectoryIfNotExists(videoStoragePath);
        createDirectoryIfNotExists(frameStoragePath);
        
        System.out.println("===========================================");
        System.out.println("Storage Configuration:");
        System.out.println("Video Path: " + videoStoragePath);
        System.out.println("Frame Path: " + frameStoragePath);
        System.out.println("===========================================");
    }

    private void createDirectoryIfNotExists(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("Created directory: " + path);
            }
        }
    }

    public String getVideoStoragePath() {
        return videoStoragePath;
    }

    public String getFrameStoragePath() {
        return frameStoragePath;
    }
}
