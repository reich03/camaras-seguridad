package com.security.camera.controller;

import com.security.camera.dto.ConnectionDTO;
import com.security.camera.service.ConnectionService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
@CrossOrigin
public class ConnectionController {

    private final ConnectionService connectionService;

    @PostMapping("/connect")
    public ResponseEntity<ConnectionDTO> connect(@RequestBody ConnectionRequest request) {
        try {
            ConnectionDTO connection = connectionService.connect(request.getUserId(), request.getIpAddress());
            return ResponseEntity.status(HttpStatus.CREATED).body(connection);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/disconnect")
    public ResponseEntity<Void> disconnect(@PathVariable Long id) {
        try {
            connectionService.disconnect(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<ConnectionDTO>> getActiveConnections() {
        List<ConnectionDTO> connections = connectionService.getActiveConnections();
        return ResponseEntity.ok(connections);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ConnectionDTO>> getUserConnections(@PathVariable Long userId) {
        List<ConnectionDTO> connections = connectionService.getUserConnections(userId);
        return ResponseEntity.ok(connections);
    }

    @PostMapping("/{id}/increment-files")
    public ResponseEntity<Void> incrementFilesSent(@PathVariable Long id) {
        try {
            connectionService.incrementFilesSent(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectionRequest {
        private Long userId;
        private String ipAddress;
    }
}
