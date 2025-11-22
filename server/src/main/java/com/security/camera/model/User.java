package com.security.camera.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(length = 150)
    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "max_connections")
    private Integer maxConnections;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Camera> cameras = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserConnection> connections = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (maxConnections == null) {
            maxConnections = 3;
        }
    }

   
    public static class UserBuilder {
        private String username;
        private String password;
        private String email;
        private Integer maxConnections;
        private Boolean isActive;

        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder maxConnections(Integer maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public UserBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public User build() {
            User user = new User();
            user.setUsername(this.username);
            user.setPassword(this.password);
            user.setEmail(this.email);
            user.setMaxConnections(this.maxConnections != null ? this.maxConnections : 3);
            user.setIsActive(this.isActive != null ? this.isActive : true);
            user.setCreatedAt(LocalDateTime.now());
            return user;
        }
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }
}
