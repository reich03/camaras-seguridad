package com.security.camera.web.controller;

import com.security.camera.web.dto.*;
import com.security.camera.web.service.ApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final ApiService apiService;

    @GetMapping("/")
    public String index(Model model) {
        try {
            List<UserDTO> users = apiService.getAllUsers();
            model.addAttribute("users", users);
            model.addAttribute("userCount", users.size());
            
            List<ConnectionDTO> activeConnections = apiService.getActiveConnections();
            model.addAttribute("activeConnections", activeConnections.size());
        } catch (Exception e) {
            model.addAttribute("error", "Error connecting to server: " + e.getMessage());
        }
        return "index";
    }

    @GetMapping("/users")
    public String users(Model model) {
        try {
            List<UserDTO> users = apiService.getAllUsers();
            
            // Enriquecer cada usuario con estadísticas
            for (UserDTO user : users) {
                try {
                    UserStatsDTO stats = apiService.getUserStats(user.getId());
                    user.setTotalCameras(stats.getTotalCameras());
                    user.setTotalVideos(stats.getTotalVideos());
                } catch (Exception e) {
                    // Si no se pueden obtener stats, dejar en 0
                }
            }
            
            model.addAttribute("users", users);
        } catch (Exception e) {
            model.addAttribute("error", "Error loading users: " + e.getMessage());
        }
        return "users";
    }

    @PostMapping("/users/create")
    public String createUser(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String email,
                           @RequestParam(defaultValue = "3") Integer maxConnections,
                           Model model) {
        try {
            apiService.registerUser(username, password, email, maxConnections);
            return "redirect:/users";
        } catch (Exception e) {
            model.addAttribute("error", "Error creating user: " + e.getMessage());
            return "redirect:/users?error=" + e.getMessage();
        }
    }

    @GetMapping("/users/{id}")
    public String userDetails(@PathVariable Long id, Model model) {
        try {
            UserDTO user = apiService.getUserById(id);
            UserStatsDTO stats = apiService.getUserStats(id);
            List<CameraDTO> cameras = apiService.getCamerasByUserId(id);
            List<VideoDTO> videos = apiService.getVideosByUserId(id);
            List<ConnectionDTO> connections = apiService.getUserConnections(id);

            model.addAttribute("user", user);
            model.addAttribute("stats", stats);
            model.addAttribute("cameras", cameras);
            model.addAttribute("videos", videos);
            model.addAttribute("connections", connections);
        } catch (Exception e) {
            model.addAttribute("error", "Error loading user details: " + e.getMessage());
        }
        return "user-details";
    }

    @GetMapping("/cameras")
    public String cameras(@RequestParam(required = false) Long userId, Model model) {
        try {
            List<CameraDTO> cameras;
            if (userId != null) {
                cameras = apiService.getCamerasByUserId(userId);
            } else {
                cameras = apiService.getAllCameras();
            }
            model.addAttribute("cameras", cameras);
            
            List<UserDTO> users = apiService.getAllUsers();
            model.addAttribute("users", users);
            model.addAttribute("selectedUserId", userId);
        } catch (Exception e) {
            model.addAttribute("error", "Error loading cameras: " + e.getMessage());
        }
        return "cameras";
    }

    @GetMapping("/videos")
    public String videos(@RequestParam(required = false) Long userId, Model model) {
        try {
            List<VideoDTO> videos;
            if (userId != null) {
                videos = apiService.getVideosByUserId(userId);
            } else {
                videos = apiService.getAllVideos();
            }
            model.addAttribute("videos", videos);
            
            List<UserDTO> users = apiService.getAllUsers();
            model.addAttribute("users", users);
            model.addAttribute("selectedUserId", userId);
        } catch (Exception e) {
            model.addAttribute("error", "Error loading videos: " + e.getMessage());
        }
        return "videos";
    }

    @GetMapping("/videos/{id}")
    public String videoDetails(@PathVariable Long id, Model model) {
        try {
            VideoDTO video = apiService.getVideoById(id);
            model.addAttribute("video", video);
            
            // Obtener frames del video
            List<FrameDTO> frames = apiService.getFramesByVideoId(id);
            model.addAttribute("frames", frames);
        } catch (Exception e) {
            model.addAttribute("error", "Error loading video: " + e.getMessage());
        }
        return "video-details";
    }

    @GetMapping("/connections")
    public String connections(Model model) {
        try {
            List<ConnectionDTO> connections = apiService.getActiveConnections();
            model.addAttribute("connections", connections);
        } catch (Exception e) {
            model.addAttribute("error", "Error loading connections: " + e.getMessage());
        }
        return "connections";
    }
}
