package com.security.camera.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad UserConnection para rastrear conexiones de usuarios
 */
@Entity
@Table(name = "user_connections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "connected_at")
    private LocalDateTime connectedAt;

    @Column(name = "disconnected_at")
    private LocalDateTime disconnectedAt;

    @Column(name = "files_sent")
    private Integer filesSent;

    @PrePersist
    protected void onCreate() {
        if (connectedAt == null) {
            connectedAt = LocalDateTime.now();
        }
        if (filesSent == null) {
            filesSent = 0;
        }
    }
}
