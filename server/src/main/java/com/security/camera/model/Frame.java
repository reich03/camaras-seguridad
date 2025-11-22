package com.security.camera.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad Frame para almacenar fotogramas extraídos de videos
 */
@Entity
@Table(name = "frames")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Frame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(name = "frame_path", nullable = false, length = 500)
    private String framePath;

    @Column(name = "frame_number")
    private Integer frameNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
