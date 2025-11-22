package com.security.camera.web.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private Boolean isActive;
    private Integer maxConnections;
    
    // Stats agregadas dinámicamente
    private Integer totalCameras;
    private Integer totalVideos;
}
