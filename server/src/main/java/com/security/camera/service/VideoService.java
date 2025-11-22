package com.security.camera.service;

import com.security.camera.config.StorageConfig;
import com.security.camera.dto.VideoDTO;
import com.security.camera.model.Camera;
import com.security.camera.model.Video;
import com.security.camera.repository.CameraRepository;
import com.security.camera.repository.FrameRepository;
import com.security.camera.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class VideoService {

    private final VideoRepository videoRepository;
    private final CameraRepository cameraRepository;
    private final FrameRepository frameRepository;
    private final StorageConfig storageConfig;
    private final VideoProcessingService videoProcessingService;

    /**
     * Subir video usando Builder Pattern
     */
    public VideoDTO uploadVideo(Long cameraId, MultipartFile file) throws IOException {
        Camera camera = cameraRepository.findById(cameraId)
                .orElseThrow(() -> new RuntimeException("Camera not found"));

        // Generar nombre único para el archivo
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".mp4";
        String filename = UUID.randomUUID().toString() + extension;
        
        // Guardar archivo
        Path videoPath = Paths.get(storageConfig.getVideoStoragePath(), filename);
        Files.createDirectories(videoPath.getParent());
        Files.copy(file.getInputStream(), videoPath, StandardCopyOption.REPLACE_EXISTING);

        // Usar Builder Pattern para crear video
        Video video = Video.builder()
                .camera(camera)
                .videoPath(videoPath.toString())
                .fileSizeBytes(file.getSize())
                .build();

        video = videoRepository.save(video);

        // Procesar video de forma asíncrona
        videoProcessingService.processVideoAsync(video.getId());

        return convertToDTO(video);
    }

    public List<VideoDTO> getAllVideos() {
        return videoRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<VideoDTO> getVideosByCameraId(Long cameraId) {
        return videoRepository.findByCameraId(cameraId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<VideoDTO> getVideosByUserId(Long userId) {
        return videoRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public VideoDTO getVideoById(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));
        return convertToDTO(video);
    }

    public File getVideoFile(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));
        File file = new File(video.getVideoPath());
        if (!file.exists()) {
            throw new RuntimeException("Video file not found");
        }
        return file;
    }

    private VideoDTO convertToDTO(Video video) {
        int frameCount = frameRepository.findByVideoId(video.getId()).size();
        
        return VideoDTO.builder()
                .id(video.getId())
                .cameraId(video.getCamera().getId())
                .cameraName(video.getCamera().getCameraName())
                .videoPath(video.getVideoPath())
                .uploadedAt(video.getUploadedAt())
                .durationSeconds(video.getDurationSeconds())
                .fileSizeBytes(video.getFileSizeBytes())
                .frameCount(frameCount)
                .build();
    }
}
