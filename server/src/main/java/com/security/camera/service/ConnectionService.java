package com.security.camera.service;

import com.security.camera.dto.ConnectionDTO;
import com.security.camera.model.User;
import com.security.camera.model.UserConnection;
import com.security.camera.repository.UserConnectionRepository;
import com.security.camera.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ConnectionService {

    private final UserConnectionRepository connectionRepository;
    private final UserRepository userRepository;

    
    public ConnectionDTO connect(Long userId, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long activeConnections = connectionRepository.countActiveConnectionsByUserId(userId);
        if (activeConnections >= user.getMaxConnections()) {
            throw new RuntimeException("Maximum connections reached for user: " + user.getMaxConnections());
        }

        UserConnection connection = UserConnection.builder()
                .user(user)
                .ipAddress(ipAddress)
                .connectedAt(LocalDateTime.now())
                .filesSent(0)
                .build();

        connection = connectionRepository.save(connection);
        return convertToDTO(connection);
    }

    
    public void disconnect(Long connectionId) {
        UserConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new RuntimeException("Connection not found"));

        connection.setDisconnectedAt(LocalDateTime.now());
        connectionRepository.save(connection);
    }

    public List<ConnectionDTO> getActiveConnections() {
        return connectionRepository.findAllActiveConnections().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    
    public List<ConnectionDTO> getUserConnections(Long userId) {
        return connectionRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    
    public void incrementFilesSent(Long connectionId) {
        UserConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new RuntimeException("Connection not found"));
        
        connection.setFilesSent(connection.getFilesSent() + 1);
        connectionRepository.save(connection);
    }

    private ConnectionDTO convertToDTO(UserConnection connection) {
        return ConnectionDTO.builder()
                .id(connection.getId())
                .userId(connection.getUser().getId())
                .username(connection.getUser().getUsername())
                .ipAddress(connection.getIpAddress())
                .connectedAt(connection.getConnectedAt())
                .disconnectedAt(connection.getDisconnectedAt())
                .filesSent(connection.getFilesSent())
                .isActive(connection.getDisconnectedAt() == null)
                .build();
    }
}
