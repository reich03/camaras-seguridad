package com.security.camera.web.service;

import com.security.camera.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApiService {

    private final RestTemplate restTemplate;

    @Value("${server.api.url}")
    private String serverApiUrl;

    // User endpoints
    public List<UserDTO> getAllUsers() {
        ResponseEntity<List<UserDTO>> response = restTemplate.exchange(
                serverApiUrl + "/api/users",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<UserDTO>>() {}
        );
        return response.getBody();
    }

    public UserDTO getUserById(Long id) {
        return restTemplate.getForObject(serverApiUrl + "/api/users/" + id, UserDTO.class);
    }

    public UserDTO registerUser(String username, String password, String email, Integer maxConnections) {
        Map<String, Object> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);
        request.put("email", email);
        request.put("maxConnections", maxConnections);
        
        return restTemplate.postForObject(serverApiUrl + "/api/users/register", request, UserDTO.class);
    }

    public UserStatsDTO getUserStats(Long id) {
        return restTemplate.getForObject(serverApiUrl + "/api/users/" + id + "/stats", UserStatsDTO.class);
    }

    // Camera endpoints
    public List<CameraDTO> getAllCameras() {
        ResponseEntity<List<CameraDTO>> response = restTemplate.exchange(
                serverApiUrl + "/api/cameras",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<CameraDTO>>() {}
        );
        return response.getBody();
    }

    public List<CameraDTO> getCamerasByUserId(Long userId) {
        ResponseEntity<List<CameraDTO>> response = restTemplate.exchange(
                serverApiUrl + "/api/cameras/user/" + userId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<CameraDTO>>() {}
        );
        return response.getBody();
    }

    // Video endpoints
    public List<VideoDTO> getAllVideos() {
        ResponseEntity<List<VideoDTO>> response = restTemplate.exchange(
                serverApiUrl + "/api/videos",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<VideoDTO>>() {}
        );
        return response.getBody();
    }

    public List<VideoDTO> getVideosByUserId(Long userId) {
        ResponseEntity<List<VideoDTO>> response = restTemplate.exchange(
                serverApiUrl + "/api/videos/user/" + userId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<VideoDTO>>() {}
        );
        return response.getBody();
    }

    public VideoDTO getVideoById(Long id) {
        return restTemplate.getForObject(serverApiUrl + "/api/videos/" + id, VideoDTO.class);
    }

    public List<FrameDTO> getFramesByVideoId(Long videoId) {
        ResponseEntity<List<FrameDTO>> response = restTemplate.exchange(
                serverApiUrl + "/api/videos/" + videoId + "/frames",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<FrameDTO>>() {}
        );
        return response.getBody();
    }

    // Connection endpoints
    public List<ConnectionDTO> getActiveConnections() {
        ResponseEntity<List<ConnectionDTO>> response = restTemplate.exchange(
                serverApiUrl + "/api/connections/active",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ConnectionDTO>>() {}
        );
        return response.getBody();
    }

    public List<ConnectionDTO> getUserConnections(Long userId) {
        ResponseEntity<List<ConnectionDTO>> response = restTemplate.exchange(
                serverApiUrl + "/api/connections/user/" + userId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ConnectionDTO>>() {}
        );
        return response.getBody();
    }
}
