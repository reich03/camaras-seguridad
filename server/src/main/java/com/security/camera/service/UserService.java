package com.security.camera.service;

import com.security.camera.dto.*;
import com.security.camera.model.User;
import com.security.camera.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CameraRepository cameraRepository;
    private final VideoRepository videoRepository;
    private final UserConnectionRepository connectionRepository;
    private final MessageRepository messageRepository;

    /**
     * Registrar nuevo usuario usando Builder Pattern
     */
    public UserDTO registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Usar Builder Pattern para crear usuario
        User user = User.builder()
                .username(request.getUsername())
                .password(encodePassword(request.getPassword())) // En producción usar BCrypt
                .email(request.getEmail())
                .maxConnections(request.getMaxConnections() != null ? request.getMaxConnections() : 3)
                .isActive(true)
                .build();

        user = userRepository.save(user);
        return convertToDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public LoginResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElse(null);
        
        if (user == null) {
            return null; // Usuario no encontrado
        }
        
        // Verificar contraseña
        if (!user.getPassword().equals(password)) {
            return null; // Contraseña incorrecta
        }
        
        if (!user.getIsActive()) {
            return null; // Usuario inactivo
        }
        
        return LoginResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .isActive(user.getIsActive())
                .maxConnections(user.getMaxConnections())
                .build();
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDTO(user);
    }

    public UserStatsDTO getUserStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long totalCameras = cameraRepository.countActiveCamerasByUserId(userId);
        long totalVideos = videoRepository.findByUserId(userId).size();
        long totalMessages = messageRepository.findByUserIdOrderBySentAtDesc(userId).size();
        long activeConnections = connectionRepository.countActiveConnectionsByUserId(userId);
        
        long totalFilesSent = connectionRepository.findByUserId(userId).stream()
                .mapToLong(conn -> conn.getFilesSent() != null ? conn.getFilesSent() : 0)
                .sum();

        return UserStatsDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .totalCameras((int) totalCameras)
                .activeCameras((int) totalCameras)
                .totalVideos((int) totalVideos)
                .totalMessages((int) totalMessages)
                .activeConnections((int) activeConnections)
                .totalFilesSent(totalFilesSent)
                .build();
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .isActive(user.getIsActive())
                .maxConnections(user.getMaxConnections())
                .build();
    }

    private String encodePassword(String password) {
        // En producción, usar BCrypt
        return password; // Simplificado para demostración
    }
}
