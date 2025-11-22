package com.security.camera.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDTO {
    private Long userId;
    private String username;
    private Integer totalCameras;
    private Integer activeCameras;
    private Integer totalVideos;
    private Integer totalMessages;
    private Integer activeConnections;
    private Long totalFilesSent;
}
