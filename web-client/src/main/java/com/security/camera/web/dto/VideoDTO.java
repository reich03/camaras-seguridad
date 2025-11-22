package com.security.camera.web.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VideoDTO {
    private Long id;
    private Long cameraId;
    private Long userId;
    private String cameraName;
    private String videoPath;
    private LocalDateTime uploadedAt;
    private Integer durationSeconds;
    private Long fileSizeBytes;
    private Integer frameCount;
}
