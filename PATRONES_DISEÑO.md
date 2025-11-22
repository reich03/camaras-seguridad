# Documentación de Patrones de Diseño Implementados

## 1. Builder Pattern (Patrón Constructor)

### Implementación en User
**Ubicación**: `server/src/main/java/com/security/camera/model/User.java`

```java
// Uso del Builder Pattern
User user = User.builder()
    .username("admin")
    .password("admin123")
    .email("admin@example.com")
    .maxConnections(5)
    .isActive(true)
    .build();
```

**Ventajas**:
- ✅ Construcción flexible de objetos complejos
- ✅ Código más legible y mantenible
- ✅ Validación durante la construcción
- ✅ Inmutabilidad opcional

### Implementación en Camera
**Ubicación**: `server/src/main/java/com/security/camera/model/Camera.java`

```java
Camera camera = Camera.builder()
    .cameraName("Entrada Principal")
    .user(user)
    .ipAddress("192.168.1.100")
    .isActive(true)
    .build();
```

### Implementación en Video
**Ubicación**: `server/src/main/java/com/security/camera/model/Video.java`

```java
Video video = Video.builder()
    .camera(camera)
    .videoPath("/app/videos/video123.mp4")
    .durationSeconds(120)
    .fileSizeBytes(15000000L)
    .build();
```

**Características**:
- Validación de campos obligatorios
- Valores por defecto automáticos
- Prevención de estados inválidos

## 2. Object Pool Pattern (Patrón Pool de Objetos)

### Implementación: Thread Pool
**Ubicación**: `server/src/main/java/com/security/camera/config/ThreadPoolConfig.java`

```java
@Bean(name = "videoProcessingExecutor")
public Executor videoProcessingExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);      // Hilos mínimos
    executor.setMaxPoolSize(20);       // Hilos máximos
    executor.setQueueCapacity(100);    // Cola de tareas
    executor.initialize();
    return executor;
}
```

**Uso en VideoProcessingService**:
```java
@Async("videoProcessingExecutor")
public void processVideoAsync(Long videoId) {
    // Procesamiento asíncrono usando el pool de threads
}
```

**Ventajas**:
- ✅ Reutilización de threads (evita crear/destruir constantemente)
- ✅ Control del uso de recursos
- ✅ Mejor rendimiento en procesamiento paralelo
- ✅ Gestión eficiente de carga

### Implementación: Connection Pool (HikariCP)
**Ubicación**: `server/src/main/resources/application.properties`

```properties
# Object Pool Pattern para conexiones a BD
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.pool-name=SecurityCameraHikariPool
```

**Ventajas**:
- ✅ Reutilización de conexiones a BD
- ✅ Reducción de latencia
- ✅ Gestión automática de conexiones
- ✅ Pool size configurable

## 3. Principios SOLID Aplicados

### Single Responsibility Principle (SRP)
Cada clase tiene una única responsabilidad:
- `UserService`: Gestión de usuarios
- `CameraService`: Gestión de cámaras
- `VideoService`: Gestión de videos
- `ConnectionService`: Gestión de conexiones
- `VideoProcessingService`: Procesamiento asíncrono

### Open/Closed Principle (OCP)
- Uso de interfaces y abstracciones
- Servicios extensibles sin modificar código existente

### Liskov Substitution Principle (LSP)
- Herencia correcta en DTOs
- Interfaces respetadas en implementaciones

### Interface Segregation Principle (ISP)
- Repositorios específicos por entidad
- DTOs segregados por funcionalidad

### Dependency Inversion Principle (DIP)
- Inyección de dependencias con Spring
- Dependencia de abstracciones, no implementaciones

```java
@Service
@RequiredArgsConstructor  // Inyección por constructor
public class VideoService {
    private final VideoRepository videoRepository;  // Abstracción
    private final CameraRepository cameraRepository;
    private final VideoProcessingService videoProcessingService;
}
```

## 4. Otros Patrones Identificables

### Repository Pattern
**Implementación**: JPA Repositories
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

### Data Transfer Object (DTO) Pattern
**Ejemplo**: `UserDTO`, `CameraDTO`, `VideoDTO`
- Separación entre capa de datos y presentación
- Prevención de over-fetching
- Optimización de transferencia de datos

### Service Layer Pattern
Capa de servicios entre controladores y repositorios:
- `UserService`
- `CameraService`
- `VideoService`
- `ConnectionService`

### MVC Pattern
**Cliente Web**:
- Model: DTOs
- View: Templates Thymeleaf
- Controller: `WebController`

## 5. Diagramas de Implementación

### Builder Pattern Flow
```
User Request
     ↓
User.builder()
     ↓
.username("admin")
     ↓
.password("pass")
     ↓
.email("email")
     ↓
.build()
     ↓
[Validation]
     ↓
User Object Created
```

### Object Pool Pattern Flow
```
Video Upload Request
     ↓
VideoService.uploadVideo()
     ↓
Save to DB
     ↓
VideoProcessingService.processVideoAsync()
     ↓
[Thread Pool] → Take available thread
     ↓
Process Video (extract frames, filters)
     ↓
[Thread Pool] → Release thread
     ↓
Thread available for next task
```

### Connection Pool Flow
```
API Request
     ↓
Repository Query
     ↓
[HikariCP] → Get connection from pool
     ↓
Execute Query
     ↓
[HikariCP] → Return connection to pool
     ↓
Connection available for next request
```

## 6. Beneficios de los Patrones Implementados

### Builder Pattern
- 📝 Código más legible
- 🔒 Validación centralizada
- 🎯 Construcción paso a paso
- ✅ Prevención de errores

### Object Pool Pattern
- ⚡ Mejor rendimiento
- 💾 Uso eficiente de memoria
- 🔄 Reutilización de recursos
- 📊 Control de concurrencia

### SOLID Principles
- 🧩 Código modular
- 🔧 Fácil mantenimiento
- 🧪 Testeable
- 📈 Escalable

## 7. Métricas de Calidad

**Thread Pool**:
- Core Pool Size: 10 threads
- Max Pool Size: 20 threads
- Queue Capacity: 100 tareas

**Connection Pool**:
- Max Pool Size: 20 conexiones
- Min Idle: 5 conexiones
- Timeout: 30 segundos

## 8. Casos de Uso Reales

### Builder Pattern
```java
// UserService.java - Registro de usuario
User user = User.builder()
    .username(request.getUsername())
    .password(encodePassword(request.getPassword()))
    .email(request.getEmail())
    .maxConnections(request.getMaxConnections() != null ? request.getMaxConnections() : 3)
    .isActive(true)
    .build();
```

### Object Pool Pattern
```java
// VideoProcessingService.java - Procesamiento asíncrono
@Async("videoProcessingExecutor")  // Usa el thread pool
@Transactional
public void processVideoAsync(Long videoId) {
    // El thread se obtiene del pool automáticamente
    List<Frame> frames = extractFrames(video);
    frameRepository.saveAll(frames);
    // El thread se devuelve al pool automáticamente
}
```

## 9. Referencias

- Design Patterns: Elements of Reusable Object-Oriented Software (Gang of Four)
- Spring Framework Documentation
- HikariCP Documentation
- Java Concurrency in Practice
