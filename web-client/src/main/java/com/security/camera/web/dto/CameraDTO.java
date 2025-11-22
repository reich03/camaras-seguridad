package com.security.camera.web.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CameraDTO {
    private Long id;
    private String cameraName;
    private Long userId;
    private String username;
    private String ipAddress;
    private LocalDateTime registeredAt;
    private Boolean isActive;
    private Integer videoCount;
}
