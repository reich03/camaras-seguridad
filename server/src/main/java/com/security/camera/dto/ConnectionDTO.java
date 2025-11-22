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
