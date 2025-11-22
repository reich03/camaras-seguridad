package com.security.camera.controller;

import com.security.camera.dto.VideoDTO;
import com.security.camera.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
@CrossOrigin
public class VideoController {

    private final VideoService videoService;

    @GetMapping
    public ResponseEntity<List<VideoDTO>> getAllVideos() {
        List<VideoDTO> videos = videoService.getAllVideos();
        return ResponseEntity.ok(videos);
    }

    @PostMapping("/upload")
    public ResponseEntity<VideoDTO> uploadVideo(
            @RequestParam("cameraId") Long cameraId,
            @RequestParam("file") MultipartFile file) {
        try {
            VideoDTO video = videoService.uploadVideo(cameraId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(video);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/camera/{cameraId}")
    public ResponseEntity<List<VideoDTO>> getVideosByCameraId(@PathVariable Long cameraId) {
        List<VideoDTO> videos = videoService.getVideosByCameraId(cameraId);
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<VideoDTO>> getVideosByUserId(@PathVariable Long userId) {
        List<VideoDTO> videos = videoService.getVideosByUserId(userId);
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoDTO> getVideoById(@PathVariable Long id) {
        try {
            VideoDTO video = videoService.getVideoById(id);
            return ResponseEntity.ok(video);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadVideo(@PathVariable Long id) {
        try {
            File file = videoService.getVideoFile(id);
            Resource resource = new FileSystemResource(file);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(file.length())
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/stream")
    public ResponseEntity<Resource> streamVideo(@PathVariable Long id) {
        try {
            File file = videoService.getVideoFile(id);
            Resource resource = new FileSystemResource(file);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .contentLength(file.length())
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/frames")
    public ResponseEntity<List<?>> getVideoFrames(@PathVariable Long id) {
        try {
            // Este método debería retornar la lista de frames
            // Por ahora retornamos una lista vacía
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
