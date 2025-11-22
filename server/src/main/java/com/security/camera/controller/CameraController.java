package com.security.camera.controller;

import com.security.camera.dto.CameraDTO;
import com.security.camera.dto.CameraRegistrationRequest;
import com.security.camera.service.CameraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cameras")
@RequiredArgsConstructor
@CrossOrigin
public class CameraController {

    private final CameraService cameraService;

    @GetMapping
    public ResponseEntity<List<CameraDTO>> getAllCameras() {
        List<CameraDTO> cameras = cameraService.getAllCameras();
        return ResponseEntity.ok(cameras);
    }

    @PostMapping("/register")
    public ResponseEntity<CameraDTO> registerCamera(@RequestBody CameraRegistrationRequest request) {
        try {
            CameraDTO camera = cameraService.registerCamera(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(camera);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CameraDTO>> getCamerasByUserId(@PathVariable Long userId) {
        List<CameraDTO> cameras = cameraService.getCamerasByUserId(userId);
        return ResponseEntity.ok(cameras);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CameraDTO> getCameraById(@PathVariable Long id) {
        try {
            CameraDTO camera = cameraService.getCameraById(id);
            return ResponseEntity.ok(camera);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<CameraDTO> activateCamera(@PathVariable Long id) {
        try {
            CameraDTO camera = cameraService.activateCamera(id);
            return ResponseEntity.ok(camera);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCamera(@PathVariable Long id) {
        try {
            cameraService.deleteCamera(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
