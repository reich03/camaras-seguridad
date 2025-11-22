package com.security.camera.desktop;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Cliente para comunicarse con la API REST del servidor
 */
public class ApiClient {
    
    private final String serverUrl;
    private final OkHttpClient client;
    private final Gson gson;

    public ApiClient(String serverUrl) {
        this.serverUrl = serverUrl;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    // User endpoints
    public List<UserDTO> getAllUsers() throws IOException {
        Request request = new Request.Builder()
                .url(serverUrl + "/api/users")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Error: " + response.code());
            String json = response.body().string();
            return gson.fromJson(json, new TypeToken<List<UserDTO>>(){}.getType());
        }
    }

    // Camera endpoints
    public List<CameraDTO> getCamerasByUserId(Long userId) throws IOException {
        Request request = new Request.Builder()
                .url(serverUrl + "/api/cameras/user/" + userId)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Error: " + response.code());
            String json = response.body().string();
            return gson.fromJson(json, new TypeToken<List<CameraDTO>>(){}.getType());
        }
    }

    public CameraDTO registerCamera(String cameraName, Long userId, String ipAddress) throws IOException {
        String jsonBody = String.format(
            "{\"cameraName\":\"%s\",\"userId\":%d,\"ipAddress\":\"%s\"}",
            cameraName, userId, ipAddress
        );

        RequestBody body = RequestBody.create(
            jsonBody,
            MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(serverUrl + "/api/cameras/register")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Error: " + response.code());
            String json = response.body().string();
            return gson.fromJson(json, CameraDTO.class);
        }
    }

    // Video endpoints
    public VideoDTO uploadVideo(Long cameraId, File file) throws IOException {
        RequestBody fileBody = RequestBody.create(file, MediaType.parse("video/*"));
        
        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("cameraId", cameraId.toString())
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(serverUrl + "/api/videos/upload")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Error: " + response.code());
            String json = response.body().string();
            return gson.fromJson(json, VideoDTO.class);
        }
    }

    // Connection endpoints
    public ConnectionDTO connect(Long userId, String ipAddress) throws IOException {
        String jsonBody = String.format(
            "{\"userId\":%d,\"ipAddress\":\"%s\"}",
            userId, ipAddress
        );

        RequestBody body = RequestBody.create(
            jsonBody,
            MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(serverUrl + "/api/connections/connect")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Error: " + response.code());
            String json = response.body().string();
            return gson.fromJson(json, ConnectionDTO.class);
        }
    }

    public void disconnect(Long connectionId) throws IOException {
        Request request = new Request.Builder()
                .url(serverUrl + "/api/connections/" + connectionId + "/disconnect")
                .post(RequestBody.create("", MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Error: " + response.code());
        }
    }

    public void incrementFilesSent(Long connectionId) throws IOException {
        Request request = new Request.Builder()
                .url(serverUrl + "/api/connections/" + connectionId + "/increment-files")
                .post(RequestBody.create("", MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Error: " + response.code());
        }
    }

    // DTOs
    public static class UserDTO {
        private Long id;
        private String username;
        private String email;

        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
    }

    public static class CameraDTO {
        private Long id;
        private String cameraName;
        private Long userId;
        private String ipAddress;

        public Long getId() { return id; }
        public String getCameraName() { return cameraName; }
        public Long getUserId() { return userId; }
        public String getIpAddress() { return ipAddress; }
    }

    public static class VideoDTO {
        private Long id;
        private Long cameraId;
        private String videoPath;

        public Long getId() { return id; }
        public Long getCameraId() { return cameraId; }
        public String getVideoPath() { return videoPath; }
    }

    public static class ConnectionDTO {
        private Long id;
        private Long userId;
        private String ipAddress;

        public Long getId() { return id; }
        public Long getUserId() { return userId; }
        public String getIpAddress() { return ipAddress; }
    }
}
