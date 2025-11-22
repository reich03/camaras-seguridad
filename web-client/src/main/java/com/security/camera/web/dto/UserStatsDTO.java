package com.security.camera.web.dto;

import lombok.Data;

@Data
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
