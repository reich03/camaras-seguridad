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
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
@CrossOrigin
public class ConnectionController {

    private final ConnectionService connectionService;
    
    // Patrón para validar direcciones IP
    private static final Pattern IP_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    @PostMapping("/connect")
    public ResponseEntity<?> connect(@RequestBody ConnectionRequest request) {
        try {
            // Validar dirección IP
            if (!isValidIpAddress(request.getIpAddress())) {
                return ResponseEntity.badRequest().body("Invalid IP address format");
            }
            
            // Validar conexión TCP-IP
            if (!isReachable(request.getIpAddress())) {
                return ResponseEntity.badRequest().body("IP address is not reachable");
            }
            
            ConnectionDTO connection = connectionService.connect(request.getUserId(), request.getIpAddress());
            return ResponseEntity.status(HttpStatus.CREATED).body(connection);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Valida el formato de una dirección IP
     */
    private boolean isValidIpAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return false;
        }
        return IP_PATTERN.matcher(ipAddress.trim()).matches();
    }
    
    /**
     * Verifica si la dirección IP es alcanzable mediante TCP-IP
     */
    private boolean isReachable(String ipAddress) {
        try {
            // Intentar hacer ping
            InetAddress inet = InetAddress.getByName(ipAddress);
            boolean reachable = inet.isReachable(3000); // 3 segundos timeout
            
            if (!reachable) {
                // Intentar conexión TCP al puerto 8082 (nuestro servidor)
                try (Socket socket = new Socket(ipAddress, 8082)) {
                    return socket.isConnected();
                } catch (Exception e) {
                    // Si no puede conectar al puerto 8082, pero hace ping, es válido
                    return false;
                }
            }
            
            return reachable;
        } catch (Exception e) {
            System.err.println("Error checking IP reachability: " + e.getMessage());
            return false;
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
