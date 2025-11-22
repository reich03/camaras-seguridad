package com.security.camera.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camera_id", nullable = false)
    private Camera camera;

    @Column(name = "video_path", nullable = false, length = 500)
    private String videoPath;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Frame> frames = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }

    
    public static class VideoBuilder {
        private Camera camera;
        private String videoPath;
        private Integer durationSeconds;
        private Long fileSizeBytes;

        public VideoBuilder camera(Camera camera) {
            this.camera = camera;
            return this;
        }

        public VideoBuilder videoPath(String videoPath) {
            this.videoPath = videoPath;
            return this;
        }

        public VideoBuilder durationSeconds(Integer durationSeconds) {
            this.durationSeconds = durationSeconds;
            return this;
        }

        public VideoBuilder fileSizeBytes(Long fileSizeBytes) {
            this.fileSizeBytes = fileSizeBytes;
            return this;
        }

        public Video build() {
            if (camera == null || videoPath == null) {
                throw new IllegalStateException("Camera and video path are required");
            }
            
            Video video = new Video();
            video.setCamera(this.camera);
            video.setVideoPath(this.videoPath);
            video.setDurationSeconds(this.durationSeconds);
            video.setFileSizeBytes(this.fileSizeBytes);
            video.setUploadedAt(LocalDateTime.now());
            return video;
        }
    }

    public static VideoBuilder builder() {
        return new VideoBuilder();
    }
}
