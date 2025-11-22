# 📋 Documentación Técnica - Sistema de Cámaras de Seguridad

**Proyecto:** Security Camera System  
**Desarrolladores:** Miguel y Brayan  
**Fecha:** Noviembre 2025  
**Versión:** 2.0

---

## 📖 Índice

1. [Descripción del Sistema](#descripción-del-sistema)
2. [Arquitectura General](#arquitectura-general)
3. [Patrones de Diseño Implementados](#patrones-de-diseño-implementados)
4. [Tecnologías Utilizadas](#tecnologías-utilizadas)
5. [Funcionalidades Principales](#funcionalidades-principales)
6. [Base de Datos](#base-de-datos)
7. [APIs REST](#apis-rest)
8. [Deployment con Docker](#deployment-con-docker)

---

## 🎯 Descripción del Sistema

El **Sistema de Cámaras de Seguridad** es una aplicación completa que permite la gestión y monitoreo de cámaras de seguridad en tiempo real. El sistema está diseñado con una arquitectura de microservicios que incluye:

### Componentes Principales

1. **Servidor Backend (Spring Boot)**
   - API REST para gestión de usuarios, cámaras y videos
   - Procesamiento asíncrono de videos
   - Extracción automática de frames
   - Gestión de conexiones activas
   - Almacenamiento persistente en MySQL

2. **Cliente Web (Spring MVC + Thymeleaf)**
   - Interfaz web moderna y responsive
   - Visualización de usuarios, cámaras y videos
   - Reproductor de videos con streaming
   - Gestión de conexiones activas
   - Creación de usuarios desde UI

3. **Cliente de Escritorio (JavaFX)**
   - Captura en vivo desde webcam
   - Grabación automática de videos (60 segundos)
   - Subida automática al servidor
   - Carga de archivos de video existentes
   - Preview en tiempo real
   - Interfaz dual (Webcam + File Upload)

### Características del Sistema

✅ **Gestión de Usuarios**
- Registro de usuarios con credenciales
- Control de conexiones máximas por usuario
- Estadísticas de cámaras y videos por usuario
- Activación/desactivación de usuarios

✅ **Gestión de Cámaras**
- Registro de cámaras por usuario
- Asignación de IP y nombre
- Estado activo/inactivo
- Conteo de videos por cámara

✅ **Gestión de Videos**
- Subida de videos en múltiples formatos (MP4, AVI, MOV, MKV)
- Almacenamiento con metadatos (duración, tamaño, frames)
- Streaming para reproducción web
- Descarga de videos
- Extracción automática de frames

✅ **Monitoreo en Tiempo Real**
- Captura desde webcam física
- Grabación automática cada 60 segundos
- Preview en vivo (30 fps)
- Conexiones activas rastreadas
- Log de actividad en tiempo real

✅ **Procesamiento Asíncrono**
- Extracción de frames en background
- Thread pool dedicado para procesamiento
- No bloquea operaciones principales

---

## 🏗️ Arquitectura General

```
┌─────────────────────────────────────────────────────────────────┐
│                    SECURITY CAMERA SYSTEM                        │
└─────────────────────────────────────────────────────────────────┘

┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
│  Desktop Client  │      │   Web Client     │      │   Mobile/Other   │
│    (JavaFX)      │      │  (Thymeleaf)     │      │    Clients       │
│                  │      │                  │      │                  │
│  • Webcam Live   │      │  • User Mgmt     │      │  • Future        │
│  • File Upload   │      │  • Video Player  │      │  • Extensible    │
│  • Auto-Record   │      │  • Dashboard     │      │                  │
└────────┬─────────┘      └────────┬─────────┘      └────────┬─────────┘
         │                         │                         │
         │                         │                         │
         └─────────────────────────┼─────────────────────────┘
                                   │
                                   │ HTTP/REST
                                   │
                ┌──────────────────▼──────────────────┐
                │      Spring Boot Server             │
                │        (Port 8082)                  │
                │                                     │
                │  ┌─────────────────────────────┐   │
                │  │   REST Controllers          │   │
                │  │  • UserController           │   │
                │  │  • CameraController         │   │
                │  │  • VideoController          │   │
                │  │  • ConnectionController     │   │
                │  └──────────┬──────────────────┘   │
                │             │                       │
                │  ┌──────────▼──────────────────┐   │
                │  │   Service Layer             │   │
                │  │  • UserService              │   │
                │  │  • CameraService            │   │
                │  │  • VideoService             │   │
                │  │  • VideoProcessingService   │   │
                │  │  • ConnectionService        │   │
                │  └──────────┬──────────────────┘   │
                │             │                       │
                │  ┌──────────▼──────────────────┐   │
                │  │   Repository Layer          │   │
                │  │  (Spring Data JPA)          │   │
                │  └──────────┬──────────────────┘   │
                └─────────────┼──────────────────────┘
                              │
                              │ JDBC
                              │
                ┌─────────────▼──────────────────┐
                │      MySQL Database            │
                │        (Port 3306)             │
                │                                │
                │  Tables:                       │
                │  • users                       │
                │  • cameras                     │
                │  • videos                      │
                │  • frames                      │
                │  • user_connections            │
                │  • messages                    │
                └────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    File Storage (Docker Volume)                  │
│  • /app/videos  → Video files (MP4, AVI, MOV, MKV)              │
│  • /app/frames  → Extracted frame images (PNG)                  │
└─────────────────────────────────────────────────────────────────┘
```

### Flujo de Datos Principal

1. **Cliente Desktop → Captura Video**
   ```
   Webcam → JavaCV → Recording (60s) → MP4 File → Upload
   ```

2. **Upload → Servidor**
   ```
   HTTP POST /api/videos/upload
   → VideoService (Builder Pattern)
   → Save to /app/videos
   → Store metadata in MySQL
   → Async: VideoProcessingService
   → Extract frames → Save to /app/frames
   ```

3. **Cliente Web → Visualización**
   ```
   HTTP GET /videos/{id}
   → VideoController
   → Stream video via /api/videos/{id}/stream
   → Display frames gallery
   ```

---

## 🎨 Patrones de Diseño Implementados

El sistema implementa múltiples patrones de diseño para garantizar código limpio, mantenible y escalable.

### 1. ✅ **Builder Pattern** (REQUERIDO)

**Descripción:** Permite construir objetos complejos paso a paso, separando la construcción de la representación.

**Implementación:**

#### User (modelo)
```java
// Archivo: server/src/main/java/com/security/camera/model/User.java
/**
 * Builder Pattern Implementation
 */
public static class UserBuilder {
    private String username;
    private String password;
    private String email;
    private Integer maxConnections = 3;

    public UserBuilder username(String username) {
        this.username = username;
        return this;
    }

    public UserBuilder password(String password) {
        this.password = password;
        return this;
    }

    public UserBuilder email(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder maxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    public User build() {
        User user = new User();
        user.username = this.username;
        user.password = this.password;
        user.email = this.email;
        user.maxConnections = this.maxConnections;
        user.isActive = true;
        user.createdAt = LocalDateTime.now();
        return user;
    }
}

public static UserBuilder builder() {
    return new UserBuilder();
}
```

**Uso en UserService:**
```java
// Archivo: server/src/main/java/com/security/camera/service/UserService.java
public UserDTO registerUser(UserRegistrationRequest request) {
    // Usar Builder Pattern para crear usuario
    User user = User.builder()
            .username(request.getUsername())
            .password(request.getPassword())
            .email(request.getEmail())
            .maxConnections(request.getMaxConnections())
            .build();

    user = userRepository.save(user);
    return convertToDTO(user);
}
```

#### Camera (modelo)
```java
// Archivo: server/src/main/java/com/security/camera/model/Camera.java
public static class CameraBuilder {
    private String cameraName;
    private User user;
    private String ipAddress;
    private Boolean isActive = true;

    public CameraBuilder cameraName(String cameraName) {
        this.cameraName = cameraName;
        return this;
    }

    public CameraBuilder user(User user) {
        this.user = user;
        return this;
    }

    public CameraBuilder ipAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    public CameraBuilder isActive(Boolean isActive) {
        this.isActive = isActive;
        return this;
    }

    public Camera build() {
        Camera camera = new Camera();
        camera.cameraName = this.cameraName;
        camera.user = this.user;
        camera.ipAddress = this.ipAddress;
        camera.isActive = this.isActive;
        camera.registeredAt = LocalDateTime.now();
        return camera;
    }
}
```

**Uso en CameraService:**
```java
// Archivo: server/src/main/java/com/security/camera/service/CameraService.java
public CameraDTO registerCamera(CameraRegistrationRequest request) {
    User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

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
```

#### Video (modelo)
```java
// Archivo: server/src/main/java/com/security/camera/model/Video.java
public static class VideoBuilder {
    private Camera camera;
    private String videoPath;
    private Integer durationSeconds;
    private Long fileSizeBytes;

    public VideoBuilder camera(Camera camera) {
        this.camera = camera;
        return this;
    }

    public VideoBuilder videoPath(String videoPath) {
        this.videoPath = videoPath;
        return this;
    }

    public VideoBuilder durationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
        return this;
    }

    public VideoBuilder fileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
        return this;
    }

    public Video build() {
        Video video = new Video();
        video.camera = this.camera;
        video.videoPath = this.videoPath;
        video.durationSeconds = this.durationSeconds;
        video.fileSizeBytes = this.fileSizeBytes;
        video.uploadedAt = LocalDateTime.now();
        return video;
    }
}
```

**Uso en VideoService:**
```java
// Archivo: server/src/main/java/com/security/camera/service/VideoService.java
public VideoDTO uploadVideo(Long cameraId, MultipartFile file) throws IOException {
    Camera camera = cameraRepository.findById(cameraId)
            .orElseThrow(() -> new RuntimeException("Camera not found"));

    // Guardar archivo
    String filename = UUID.randomUUID().toString() + ".mp4";
    Path videoPath = Paths.get(storageConfig.getVideoStoragePath(), filename);
    Files.copy(file.getInputStream(), videoPath, StandardCopyOption.REPLACE_EXISTING);

    // Usar Builder Pattern para crear video
    Video video = Video.builder()
            .camera(camera)
            .videoPath(videoPath.toString())
            .fileSizeBytes(file.getSize())
            .build();

    video = videoRepository.save(video);
    return convertToDTO(video);
}
```

**Beneficios:**
- ✅ Código más legible y expresivo
- ✅ Construcción paso a paso con validación
- ✅ Inmutabilidad controlada
- ✅ Valores por defecto claros
- ✅ Facilita testing con diferentes configuraciones

---

### 2. ✅ **Object Pool Pattern** (REQUERIDO)

**Descripción:** Reutiliza objetos costosos de crear, manteniendo un pool de instancias disponibles.

**Implementación 1: Connection Pool (HikariCP)**

```properties
# Archivo: server/src/main/resources/application.properties
# Connection Pool Configuration (HikariCP - Object Pool Pattern)
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.pool-name=SecurityCameraHikariPool
```

**¿Cómo funciona?**
- HikariCP mantiene un pool de 5-20 conexiones a MySQL
- Cuando un service necesita acceso a BD, toma una conexión del pool
- Al terminar, la conexión se devuelve al pool (no se cierra)
- Evita el costo de crear/destruir conexiones constantemente
- Mejora el rendimiento hasta 10x vs crear conexiones nuevas

**Implementación 2: Thread Pool para Procesamiento de Videos**

```java
// Archivo: server/src/main/java/com/security/camera/config/ThreadPoolConfig.java
/**
 * Configuración del Thread Pool (Object Pool Pattern)
 * Reutiliza threads para procesamiento asíncrono de videos
 */
@Configuration
public class ThreadPoolConfig {

    @Value("${thread.pool.core-size:10}")
    private int corePoolSize;

    @Value("${thread.pool.max-size:20}")
    private int maxPoolSize;

    @Value("${thread.pool.queue-capacity:100}")
    private int queueCapacity;

    /**
     * Bean de Thread Pool Executor
     * Implementa Object Pool Pattern para reutilizar threads
     */
    @Bean(name = "videoProcessingExecutor")
    public Executor videoProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("video-processing-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        System.out.println("Thread Pool Configuration (Object Pool Pattern):");
        System.out.println("  Core Pool Size: " + corePoolSize);
        System.out.println("  Max Pool Size: " + maxPoolSize);
        System.out.println("  Queue Capacity: " + queueCapacity);

        return executor;
    }
}
```

**Uso en VideoProcessingService:**
```java
// Archivo: server/src/main/java/com/security/camera/service/VideoProcessingService.java
/**
 * Servicio para procesamiento asíncrono de videos
 * Utiliza el Thread Pool configurado (Object Pool Pattern)
 */
@Service
@RequiredArgsConstructor
public class VideoProcessingService {

    private final VideoRepository videoRepository;
    private final FrameRepository frameRepository;
    private final StorageConfig storageConfig;

    /**
     * Procesa el video de forma asíncrona
     * El thread es tomado del pool y devuelto al terminar
     */
    @Async("videoProcessingExecutor")
    public void processVideoAsync(Long videoId) {
        try {
            Video video = videoRepository.findById(videoId)
                    .orElseThrow(() -> new RuntimeException("Video not found"));

            // Extraer frames del video
            extractFrames(video);
            
            System.out.println("Video " + videoId + " procesado exitosamente");
        } catch (Exception e) {
            System.err.println("Error procesando video " + videoId + ": " + e.getMessage());
        }
    }
}
```

**Beneficios:**
- ✅ Reutilización de recursos costosos (conexiones, threads)
- ✅ Mejor rendimiento y throughput
- ✅ Control de recursos limitados
- ✅ Previene agotamiento de memoria
- ✅ Manejo graceful de carga alta

---

### 3. 🎯 **Repository Pattern** (ADICIONAL)

**Descripción:** Abstrae el acceso a datos, separando la lógica de negocio de la persistencia.

**Implementación con Spring Data JPA:**

```java
// Archivo: server/src/main/java/com/security/camera/repository/UserRepository.java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

// Archivo: server/src/main/java/com/security/camera/repository/CameraRepository.java
@Repository
public interface CameraRepository extends JpaRepository<Camera, Long> {
    List<Camera> findByUserId(Long userId);
    boolean existsByCameraNameAndUserId(String cameraName, Long userId);
}

// Archivo: server/src/main/java/com/security/camera/repository/VideoRepository.java
@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByCameraId(Long cameraId);
    @Query("SELECT v FROM Video v WHERE v.camera.user.id = :userId ORDER BY v.uploadedAt DESC")
    List<Video> findByUserId(@Param("userId") Long userId);
    long countByCameraId(Long cameraId);
}
```

**Beneficios:**
- ✅ Abstracción de la capa de datos
- ✅ Testing más fácil (mocking)
- ✅ Código más limpio en services
- ✅ Queries reutilizables

---

### 4. 🎯 **Service Layer Pattern** (ADICIONAL)

**Descripción:** Encapsula la lógica de negocio en servicios reutilizables.

**Implementación:**

```java
// Archivo: server/src/main/java/com/security/camera/service/UserService.java
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    
    public UserDTO registerUser(UserRegistrationRequest request) { /* ... */ }
    public List<UserDTO> getAllUsers() { /* ... */ }
    public UserDTO getUserById(Long id) { /* ... */ }
    public UserStatsDTO getUserStats(Long id) { /* ... */ }
}
```

**Beneficios:**
- ✅ Separación de responsabilidades
- ✅ Lógica de negocio centralizada
- ✅ Reutilización de código
- ✅ Testing unitario facilitado

---

### 5. 🎯 **DTO Pattern** (ADICIONAL)

**Descripción:** Objetos de transferencia de datos que separan la representación interna de la externa.

**Implementación:**

```java
// Archivo: server/src/main/java/com/security/camera/dto/UserDTO.java
@Data
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private Boolean isActive;
    private Integer maxConnections;
}

// Archivo: server/src/main/java/com/security/camera/dto/VideoDTO.java
@Data
@Builder
public class VideoDTO {
    private Long id;
    private Long cameraId;
    private Long userId;
    private String cameraName;
    private String videoPath;
    private LocalDateTime uploadedAt;
    private Integer durationSeconds;
    private Long fileSizeBytes;
    private Integer frameCount;
}
```

**Beneficios:**
- ✅ Expone solo datos necesarios
- ✅ Versionado de API más fácil
- ✅ Seguridad (no expone password, etc.)
- ✅ Serialización/deserialización controlada

---

### 6. 🎯 **MVC Pattern** (ADICIONAL)

**Descripción:** Separa aplicación en Modelo, Vista y Controlador.

**Implementación en Web Client:**

```java
// Controlador (C)
@Controller
public class WebController {
    @GetMapping("/users")
    public String users(Model model) {
        List<UserDTO> users = apiService.getAllUsers();
        model.addAttribute("users", users);
        return "users"; // Vista
    }
}

// Vista (V) - Thymeleaf Template
<!-- users.html -->
<div th:each="user : ${users}">
    <h3 th:text="${user.username}">Username</h3>
</div>

// Modelo (M) - DTOs y Entities
```

**Beneficios:**
- ✅ Separación de concerns
- ✅ Testing independiente
- ✅ Múltiples vistas para mismo modelo
- ✅ Mantenibilidad

---

### 7. 🎯 **Async Processing Pattern** (ADICIONAL)

**Descripción:** Procesamiento asíncrono para operaciones largas sin bloquear.

**Implementación:**

```java
@Async("videoProcessingExecutor")
public void processVideoAsync(Long videoId) {
    // Procesamiento en background
    extractFrames(video);
    calculateDuration(video);
}
```

**Beneficios:**
- ✅ Mejor experiencia de usuario
- ✅ No bloquea thread principal
- ✅ Escalabilidad mejorada
- ✅ Utiliza thread pool (Object Pool)

---

## 🛠️ Tecnologías Utilizadas

### Backend (Server)
- **Java 17** - Lenguaje de programación
- **Spring Boot 3.2.0** - Framework principal
- **Spring Data JPA** - ORM y repositorios
- **Hibernate** - Implementación JPA
- **MySQL 8.0** - Base de datos relacional
- **HikariCP** - Connection pooling
- **Lombok** - Reducción de boilerplate
- **Jakarta Validation** - Validación de datos
- **JavaCV 1.5.9** - Procesamiento de video/frames

### Frontend Web (Web Client)
- **Spring Boot 3.2.0** - Backend del cliente
- **Thymeleaf** - Motor de plantillas
- **Spring MVC** - Patrón MVC
- **HTML5/CSS3** - Maquetación
- **JavaScript (Vanilla)** - Interactividad

### Desktop Client
- **JavaFX 21** - Framework de UI
- **JavaCV 1.5.9** - Captura de webcam
- **FFmpeg** - Codificación de video
- **OkHttp 4.12.0** - Cliente HTTP
- **Gson 2.10.1** - Serialización JSON

### DevOps & Deployment
- **Docker** - Containerización
- **Docker Compose** - Orquestación
- **Maven 3.9** - Build tool
- **Git** - Control de versiones

---

## 💡 Funcionalidades Principales

### 1. Gestión de Usuarios
- ✅ Registro con username, password, email
- ✅ Configuración de conexiones máximas
- ✅ Activación/desactivación
- ✅ Vista de estadísticas (cámaras, videos)
- ✅ Creación desde interfaz web

### 2. Gestión de Cámaras
- ✅ Registro por usuario
- ✅ Asignación de nombre e IP
- ✅ Estado activo/inactivo
- ✅ Listado con contador de videos
- ✅ Filtrado por usuario

### 3. Gestión de Videos
- ✅ Subida manual (cliente desktop)
- ✅ Grabación automática desde webcam (60s)
- ✅ Formatos: MP4, AVI, MOV, MKV
- ✅ Extracción automática de frames
- ✅ Streaming para reproducción web
- ✅ Descarga de videos
- ✅ Metadatos: duración, tamaño, frames

### 4. Captura en Tiempo Real
- ✅ Preview de webcam a 30fps
- ✅ Grabación automática cada 60s
- ✅ Subida automática post-grabación
- ✅ Visualización de estado en UI
- ✅ Log de actividad en tiempo real

### 5. Procesamiento de Videos
- ✅ Extracción de frames asíncrona
- ✅ Thread pool dedicado
- ✅ No bloquea otras operaciones
- ✅ Almacenamiento organizado

### 6. Monitoreo y Estadísticas
- ✅ Dashboard con resumen
- ✅ Conexiones activas
- ✅ Videos por usuario/cámara
- ✅ Archivos enviados
- ✅ Estado de conexión

---

## 🗄️ Base de Datos

### Esquema de Tablas

```sql
-- Users Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    max_connections INT DEFAULT 3
);

-- Cameras Table
CREATE TABLE cameras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    camera_name VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    ip_address VARCHAR(50),
    registered_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_camera_per_user (camera_name, user_id)
);

-- Videos Table
CREATE TABLE videos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    camera_id BIGINT NOT NULL,
    video_path VARCHAR(500) NOT NULL,
    uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    duration_seconds INT,
    file_size_bytes BIGINT,
    FOREIGN KEY (camera_id) REFERENCES cameras(id) ON DELETE CASCADE
);

-- Frames Table
CREATE TABLE frames (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    video_id BIGINT NOT NULL,
    frame_number INT NOT NULL,
    image_path VARCHAR(500) NOT NULL,
    extracted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE CASCADE
);

-- User Connections Table
CREATE TABLE user_connections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    ip_address VARCHAR(50),
    connected_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    disconnected_at DATETIME,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Messages Table
CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);
```

### Relaciones

```
users (1) ←──→ (N) cameras
cameras (1) ←──→ (N) videos
videos (1) ←──→ (N) frames
users (1) ←──→ (N) user_connections
users (1) ←──→ (N) messages (sender)
users (1) ←──→ (N) messages (receiver)
```

---

## 🌐 APIs REST

### User Endpoints

```http
GET    /api/users                    # Obtener todos los usuarios
GET    /api/users/{id}               # Obtener usuario por ID
POST   /api/users/register           # Registrar nuevo usuario
GET    /api/users/{id}/stats         # Estadísticas del usuario
```

### Camera Endpoints

```http
GET    /api/cameras                  # Obtener todas las cámaras
GET    /api/cameras/{id}             # Obtener cámara por ID
POST   /api/cameras/register         # Registrar nueva cámara
GET    /api/cameras/user/{userId}    # Cámaras de un usuario
PUT    /api/cameras/{id}/activate    # Activar cámara
DELETE /api/cameras/{id}             # Eliminar cámara
```

### Video Endpoints

```http
GET    /api/videos                   # Obtener todos los videos
GET    /api/videos/{id}              # Obtener video por ID
POST   /api/videos/upload            # Subir nuevo video
GET    /api/videos/camera/{cameraId} # Videos de una cámara
GET    /api/videos/user/{userId}     # Videos de un usuario
GET    /api/videos/{id}/download     # Descargar video
GET    /api/videos/{id}/stream       # Stream video (reproducción)
GET    /api/videos/{id}/frames       # Frames del video
```

### Connection Endpoints

```http
POST   /api/connections/connect      # Conectar usuario
POST   /api/connections/disconnect   # Desconectar usuario
GET    /api/connections/active       # Conexiones activas
GET    /api/connections/user/{userId} # Conexiones de usuario
```

---

## 🐳 Deployment con Docker

### Docker Compose Structure

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: security-camera-db
    ports:
      - "3307:3306"
    environment:
      MYSQL_ROOT_PASSWORD: rootpass123
      MYSQL_DATABASE: security_camera_db
      MYSQL_USER: camuser
      MYSQL_PASSWORD: campass123
    volumes:
      - mysql_data:/var/lib/mysql
      - ./init-db.sql:/docker-entrypoint-initdb.d/init-db.sql

  server:
    build:
      context: ./server
    container_name: security-camera-server
    ports:
      - "8082:8080"
    depends_on:
      - mysql
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/security_camera_db
      SPRING_DATASOURCE_USERNAME: camuser
      SPRING_DATASOURCE_PASSWORD: campass123
    volumes:
      - video_storage:/app/videos
      - frame_storage:/app/frames

  web-client:
    build:
      context: ./web-client
    container_name: security-camera-web
    ports:
      - "8081:8081"
    depends_on:
      - server
    environment:
      SERVER_API_URL: http://server:8080

volumes:
  mysql_data:
  video_storage:
  frame_storage:
```

### Comandos de Deployment

```powershell
# Iniciar todo el sistema
docker compose up -d

# Ver logs
docker logs security-camera-server
docker logs security-camera-web

# Detener sistema
docker compose down

# Rebuild completo
docker compose up --build -d

# Ver estado
docker compose ps
```

---

## 📊 Resumen de Patrones de Diseño

| Patrón | Tipo | Implementación | Archivos Clave |
|--------|------|----------------|----------------|
| **Builder** | ✅ REQUERIDO | User, Camera, Video entities | `model/User.java`, `model/Camera.java`, `model/Video.java` |
| **Object Pool** | ✅ REQUERIDO | HikariCP + ThreadPool | `application.properties`, `config/ThreadPoolConfig.java` |
| Repository | ADICIONAL | Spring Data JPA | `repository/*.java` |
| Service Layer | ADICIONAL | Business logic separation | `service/*.java` |
| DTO | ADICIONAL | Data transfer objects | `dto/*.java` |
| MVC | ADICIONAL | Web client architecture | `web/controller/WebController.java` |
| Async | ADICIONAL | Video processing | `service/VideoProcessingService.java` |

---