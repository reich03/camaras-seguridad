# Sistema de Gestión de Cámaras de Seguridad

Sistema distribuido para gestión de cámaras de seguridad con arquitectura cliente-servidor.

## 🏗️ Arquitectura

- **Servidor**: Spring Boot REST API (Puerto 8080)
- **Cliente Web**: Spring Boot MVC + Thymeleaf (Puerto 8081)
- **Cliente Escritorio**: JavaFX (para gestión de cámaras)
- **Base de Datos**: MySQL 8.0

## 🎨 Patrones de Diseño Implementados

### Builder Pattern
- `User.UserBuilder`: Construcción flexible de usuarios
- `Camera.CameraBuilder`: Construcción de cámaras
- `Video.VideoBuilder`: Construcción de videos con validaciones

### Object Pool Pattern
- `DatabaseConnectionPool`: Pool de conexiones a BD (configurado en Spring)
- `VideoProcessingThreadPool`: Pool de hilos para procesamiento de videos
- Configuración optimizada de recursos compartidos

### Principios SOLID
- **S**: Cada clase tiene una responsabilidad única
- **O**: Uso de interfaces y herencia
- **L**: Principio de sustitución de Liskov
- **I**: Interfaces segregadas
- **D**: Inyección de dependencias con Spring

## 🚀 Inicio Rápido con Docker

### Prerrequisitos
- Docker Desktop instalado
- Java 17+ (para desarrollo local)
- Maven 3.8+ (para compilación local)

### Levantar el sistema completo

```powershell
# Navegar al directorio del proyecto
cd "d:\Trabajo\Proyecto miguel brayan\security-camera-system"

# Construir y levantar todos los servicios
docker-compose up -d --build

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down

# Detener y limpiar volúmenes
docker-compose down -v
```

### URLs de Acceso

- **API REST (Servidor)**: http://localhost:8080
- **Aplicación Web**: http://localhost:8081
- **MySQL**: localhost:3306

### Credenciales por defecto

**Usuarios de prueba**:
- Username: `admin` / Password: `admin123`
- Username: `user1` / Password: `admin123`
- Username: `user2` / Password: `admin123`

**Base de datos**:
- User: `camuser`
- Password: `campass123`
- Database: `security_camera_db`

## 📁 Estructura del Proyecto

```
security-camera-system/
├── server/                    # Servidor Spring Boot
│   ├── src/main/java/
│   │   └── com/security/camera/
│   │       ├── config/       # Configuraciones, pools
│   │       ├── controller/   # REST Controllers
│   │       ├── model/        # Entidades JPA con Builders
│   │       ├── repository/   # Repositorios JPA
│   │       ├── service/      # Lógica de negocio
│   │       └── dto/          # Data Transfer Objects
│   ├── Dockerfile
│   └── pom.xml
├── web-client/               # Cliente Web MVC
│   ├── src/main/java/
│   │   └── com/security/camera/web/
│   ├── src/main/resources/
│   │   └── templates/       # Vistas Thymeleaf
│   ├── Dockerfile
│   └── pom.xml
├── desktop-client/           # Cliente JavaFX Mejorado
│   ├── src/main/java/
│   │   └── com/security/camera/desktop/
│   │       ├── CameraClientApplication.java    # UI mejorada con tabs
│   │       ├── WebcamCaptureService.java       # Servicio de webcam
│   │       └── ApiClient.java                   # Cliente HTTP
│   ├── src/main/resources/
│   │   └── styles.css                           # Estilos CSS
│   ├── Dockerfile (opcional)
│   └── pom.xml
├── docker-compose.yml
├── init-db.sql
└── README.md
```

## 🛠️ Desarrollo Local

### Compilar el Servidor
```powershell
cd server
mvn clean package
java -jar target/security-camera-server-1.0.0.jar
```

### Compilar el Cliente Web
```powershell
cd web-client
mvn clean package
java -jar target/security-camera-web-1.0.0.jar
```

### Compilar el Cliente Escritorio
```powershell
cd desktop-client
mvn clean javafx:run
```

## 📋 Funcionalidades Principales

### Servidor (REST API)

#### Gestión de Usuarios
- `POST /api/users/register` - Registrar usuario
- `GET /api/users` - Listar usuarios
- `GET /api/users/{id}` - Detalle de usuario
- `GET /api/users/{id}/stats` - Estadísticas completas

#### Gestión de Cámaras
- `POST /api/cameras/register` - Registrar cámara
- `GET /api/cameras/user/{userId}` - Cámaras por usuario
- `PUT /api/cameras/{id}/activate` - Activar cámara
- `DELETE /api/cameras/{id}` - Eliminar cámara

#### Gestión de Videos
- `POST /api/videos/upload` - Subir video
- `GET /api/videos/camera/{cameraId}` - Videos por cámara
- `GET /api/videos/{id}/download` - Descargar video
- `GET /api/videos/user/{userId}` - Videos por usuario

#### Conexiones
- `POST /api/connections/connect` - Registrar conexión
- `POST /api/connections/disconnect` - Desconectar
- `GET /api/connections/active` - Conexiones activas

### Cliente Web

- Dashboard con estadísticas
- Gestión de usuarios (CRUD)
- Gestión de cámaras por usuario
- Visualización de archivos y videos
- Listado de conexiones activas/históricas
- Descarga de archivos

### Cliente Escritorio (JavaFX) - ✨ MEJORADO

**Interfaz con Tabs:**
- 📹 **Tab Webcam Capture**: Captura en vivo desde webcam
  - Preview en tiempo real de la webcam
  - Grabación automática cada 60 segundos
  - Subida automática al servidor
  - Barra de progreso con estado de grabación
  
- 📁 **Tab File Upload**: Selección de archivos
  - Explorador de archivos para videos (.mp4, .avi, .mov, .mkv)
  - Envío único o automático cada 60 segundos
  - Información del archivo seleccionado

**Características:**
- Registro de nuevas cámaras
- Selección de usuario y cámara
- Preview en vivo de webcam (640x480)
- Log de actividad en tiempo real con timestamps
- Estado de conexión visible
- Diseño moderno con gradientes y sombras
- Manejo de errores con diálogos informativos

## 🔒 Seguridad

- Restricción de conexiones simultáneas por usuario
- Validación de archivos enviados
- Almacenamiento seguro de contraseñas (BCrypt)
- Validación de permisos en API

## 📊 Base de Datos

### Tablas Principales
- `users` - Usuarios del sistema
- `cameras` - Cámaras registradas
- `videos` - Videos almacenados
- `frames` - Fotogramas extraídos
- `user_connections` - Historial de conexiones
- `messages` - Mensajes del sistema

## 🧪 Testing

```powershell
# Ejecutar tests del servidor
cd server
mvn test

# Ejecutar tests del cliente web
cd web-client
mvn test
```

## 📝 Notas Técnicas

- Videos se almacenan en `/app/videos` dentro del contenedor
- Frames se almacenan en `/app/frames`
- Procesamiento de videos incluye: extracción de frames, aplicación de filtros
- Filtros disponibles: escala de grises, reducción de tamaño, brillo, rotación
- Sistema de cache implementado para imágenes procesadas

## 🤝 Contribución

Desarrollado por Miguel y Brayan para el proyecto de Patrones de Diseño.

## 📄 Licencia

Proyecto académico - Todos los derechos reservados.
