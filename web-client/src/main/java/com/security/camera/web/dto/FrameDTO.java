package com.security.camera.web.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FrameDTO {
    private Long id;
    private Long videoId;
    private Integer frameNumber;
    private String imageUrl;
    private LocalDateTime extractedAt;
}
