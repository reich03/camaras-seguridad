package com.security.camera.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "cameras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Camera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "camera_name", nullable = false, length = 150)
    private String cameraName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(mappedBy = "camera", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Video> videos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    
    public static class CameraBuilder {
        private String cameraName;
        private User user;
        private String ipAddress;
        private Boolean isActive;

        public CameraBuilder cameraName(String cameraName) {
            this.cameraName = cameraName;
            return this;
        }

        public CameraBuilder user(User user) {
            this.user = user;
            return this;
        }

        public CameraBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public CameraBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Camera build() {
            if (cameraName == null || user == null) {
                throw new IllegalStateException("Camera name and user are required");
            }
            
            Camera camera = new Camera();
            camera.setCameraName(this.cameraName);
            camera.setUser(this.user);
            camera.setIpAddress(this.ipAddress);
            camera.setIsActive(this.isActive != null ? this.isActive : true);
            camera.setRegisteredAt(LocalDateTime.now());
            return camera;
        }
    }

    public static CameraBuilder builder() {
        return new CameraBuilder();
    }
}
