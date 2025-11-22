package com.security.camera.web.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConnectionDTO {
    private Long id;
    private Long userId;
    private String username;
    private String ipAddress;
    private LocalDateTime connectedAt;
    private LocalDateTime disconnectedAt;
    private Integer filesSent;
    private Boolean isActive;
}
