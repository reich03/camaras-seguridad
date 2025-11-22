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
