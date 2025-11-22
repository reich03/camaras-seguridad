package com.security.camera.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CameraRegistrationRequest {
    private String cameraName;
    private Long userId;
    private String ipAddress;
}
