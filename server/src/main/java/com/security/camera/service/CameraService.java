package com.security.camera.service;

import com.security.camera.dto.CameraDTO;
import com.security.camera.dto.CameraRegistrationRequest;
import com.security.camera.model.Camera;
import com.security.camera.model.User;
import com.security.camera.repository.CameraRepository;
import com.security.camera.repository.UserRepository;
import com.security.camera.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CameraService {

    private final CameraRepository cameraRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    /**
     * Registrar cámara usando Builder Pattern
     */
    public CameraDTO registerCamera(CameraRegistrationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (cameraRepository.existsByCameraNameAndUserId(request.getCameraName(), request.getUserId())) {
            throw new RuntimeException("Camera name already exists for this user");
        }

        // Usar Builder Pattern para crear cámara
        Camera camera = Camera.builder()
                .cameraName(request.getCameraName())
                .user(user)
                .ipAddress(request.getIpAddress())
                .isActive(true)
                .build();

        camera = cameraRepository.save(camera);
        return convertToDTO(camera);
    }

    public List<CameraDTO> getAllCameras() {
        return cameraRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CameraDTO> getCamerasByUserId(Long userId) {
        return cameraRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CameraDTO getCameraById(Long id) {
        Camera camera = cameraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Camera not found"));
        return convertToDTO(camera);
    }

    public CameraDTO activateCamera(Long id) {
        Camera camera = cameraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Camera not found"));
        camera.setIsActive(true);
        camera = cameraRepository.save(camera);
        return convertToDTO(camera);
    }

    public void deleteCamera(Long id) {
        if (!cameraRepository.existsById(id)) {
            throw new RuntimeException("Camera not found");
        }
        cameraRepository.deleteById(id);
    }

    private CameraDTO convertToDTO(Camera camera) {
        int videoCount = (int) videoRepository.countByCameraId(camera.getId());
        
        return CameraDTO.builder()
                .id(camera.getId())
                .cameraName(camera.getCameraName())
                .userId(camera.getUser().getId())
                .username(camera.getUser().getUsername())
                .ipAddress(camera.getIpAddress())
                .registeredAt(camera.getRegisteredAt())
                .isActive(camera.getIsActive())
                .videoCount(videoCount)
                .build();
    }
}
