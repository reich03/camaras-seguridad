package com.security.camera.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDTO {
    private Long id;
    private Long cameraId;
    private String cameraName;
    private String videoPath;
    private LocalDateTime uploadedAt;
    private Integer durationSeconds;
    private Long fileSizeBytes;
    private Integer frameCount;
}
