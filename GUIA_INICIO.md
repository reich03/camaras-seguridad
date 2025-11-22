# Guía de Inicio - Sistema de Gestión de Cámaras de Seguridad

## 🚀 Levantar el Sistema con Docker

### Paso 1: Verificar Prerrequisitos
```powershell
# Verificar que Docker Desktop esté corriendo
docker --version
docker-compose --version
```

### Paso 2: Construir y Levantar los Servicios
```powershell
# Navegar al directorio del proyecto
cd "d:\Trabajo\Proyecto miguel brayan\security-camera-system"

# Construir y levantar todos los servicios
docker-compose up -d --build

# Ver logs en tiempo real
docker-compose logs -f
```

### Paso 3: Verificar que los Servicios Estén Corriendo
```powershell
# Ver estado de los contenedores
docker-compose ps

# Deberías ver:
# - security-camera-db (MySQL)
# - security-camera-server (API REST)
# - security-camera-web (Cliente Web)
```

### Paso 4: Acceder a las Aplicaciones

**API REST (Servidor)**
- URL: http://localhost:8080
- Endpoints disponibles:
  - GET http://localhost:8080/api/users
  - GET http://localhost:8080/api/cameras/user/1
  - POST http://localhost:8080/api/videos/upload

**Cliente Web (Administración)**
- URL: http://localhost:8081
- Usuario de prueba: `admin` / Password: `admin123`

**Base de Datos MySQL**
- Host: localhost:3306
- Database: security_camera_db
- User: camuser
- Password: campass123

## 🖥️ Ejecutar el Cliente de Escritorio (JavaFX)

### Opción 1: Con Maven
```powershell
cd desktop-client
mvn clean javafx:run
```

### Opción 2: Compilar y ejecutar JAR
```powershell
cd desktop-client
mvn clean package
java -jar target/security-camera-desktop-1.0.0.jar
```

## 📋 Flujo de Uso Completo

### 1. Verificar que el servidor esté corriendo
```powershell
# Probar endpoint de usuarios
curl http://localhost:8080/api/users
```

### 2. Abrir el Cliente Web
- Ir a http://localhost:8081
- Ver dashboard con usuarios registrados
- Explorar cámaras y videos

### 3. Usar el Cliente de Escritorio
1. Ejecutar la aplicación JavaFX
2. Seleccionar un usuario de la lista
3. Registrar una nueva cámara:
   - Nombre: "Cámara Entrada"
   - IP: "192.168.1.100"
4. Seleccionar la cámara registrada
5. Click en "Start Sending Videos"
6. Seleccionar un archivo de video (mp4, avi, mov)
7. El video se enviará automáticamente cada 60 segundos

### 4. Verificar en el Cliente Web
- Refrescar http://localhost:8081/users/1
- Ver las cámaras registradas
- Ver los videos subidos
- Ver las conexiones activas

## 🛠️ Comandos Útiles

### Docker
```powershell
# Ver logs de un servicio específico
docker-compose logs -f server
docker-compose logs -f web-client
docker-compose logs -f mysql

# Reiniciar un servicio
docker-compose restart server

# Detener todos los servicios
docker-compose down

# Detener y eliminar volúmenes (limpieza completa)
docker-compose down -v

# Reconstruir una imagen específica
docker-compose build server
docker-compose up -d server
```

### Base de Datos
```powershell
# Conectarse a MySQL
docker exec -it security-camera-db mysql -ucamuser -pcampass123 security_camera_db

# Dentro de MySQL:
# Ver usuarios
SELECT * FROM users;

# Ver cámaras
SELECT * FROM cameras;

# Ver videos
SELECT * FROM videos;

# Ver conexiones activas
SELECT * FROM user_connections WHERE disconnected_at IS NULL;
```

### Logs y Debugging
```powershell
# Ver logs del servidor
docker-compose logs -f server | Select-String "ERROR"

# Ver espacio usado por volúmenes
docker volume ls
docker volume inspect security-camera-system_video-storage

# Entrar al contenedor del servidor
docker exec -it security-camera-server sh
# Navegar a /app/videos para ver videos
# Navegar a /app/frames para ver frames
```

## 🔧 Solución de Problemas

### El servidor no inicia
```powershell
# Verificar que MySQL esté listo
docker-compose logs mysql

# Reiniciar servicios
docker-compose restart
```

### Error de conexión en el cliente de escritorio
- Verificar que el servidor esté corriendo en http://localhost:8080
- Verificar firewall de Windows
- Cambiar la URL en ApiClient si es necesario

### No se pueden subir videos
- Verificar que el directorio /app/videos existe en el contenedor
- Verificar límite de tamaño de archivo (500MB configurado)
- Ver logs del servidor para errores específicos

### Base de datos no inicializa
```powershell
# Eliminar volúmenes y recrear
docker-compose down -v
docker-compose up -d --build
```

## 📊 Probar la API con cURL

### Registrar Usuario
```powershell
curl -X POST http://localhost:8080/api/users/register `
  -H "Content-Type: application/json" `
  -d '{\"username\":\"testuser\",\"password\":\"test123\",\"email\":\"test@example.com\"}'
```

### Registrar Cámara
```powershell
curl -X POST http://localhost:8080/api/cameras/register `
  -H "Content-Type: application/json" `
  -d '{\"cameraName\":\"Camera Test\",\"userId\":1,\"ipAddress\":\"192.168.1.200\"}'
```

### Conectar Usuario
```powershell
curl -X POST http://localhost:8080/api/connections/connect `
  -H "Content-Type: application/json" `
  -d '{\"userId\":1,\"ipAddress\":\"192.168.1.100\"}'
```

### Ver Estadísticas de Usuario
```powershell
curl http://localhost:8080/api/users/1/stats
```

## 🎯 Características Implementadas

✅ **Patrones de Diseño**
- Builder Pattern: User, Camera, Video
- Object Pool Pattern: Thread Pool para procesamiento, HikariCP para BD

✅ **Principios SOLID**
- Single Responsibility: Cada clase tiene una responsabilidad
- Open/Closed: Uso de interfaces
- Dependency Inversion: Inyección de dependencias con Spring

✅ **Funcionalidades**
- Registro de usuarios y cámaras
- Subida de videos cada 60 segundos
- Procesamiento asíncrono de videos
- Extracción de frames con filtros
- Control de conexiones simultáneas
- API REST completa
- Cliente web de administración
- Cliente de escritorio JavaFX

## 📝 Notas Importantes

1. La primera vez que se levantan los contenedores, puede tardar varios minutos en descargar las imágenes base
2. Los videos se almacenan en volúmenes Docker persistentes
3. La contraseña de los usuarios de prueba es `admin123` (sin encriptar para simplificar)
4. En producción, se debe usar BCrypt para las contraseñas
5. El procesamiento de videos requiere memoria, ajustar según necesidad

## 🆘 Soporte

Para problemas o dudas:
1. Verificar logs con `docker-compose logs -f`
2. Revisar que todos los puertos estén disponibles (3306, 8080, 8081)
3. Verificar que Docker Desktop tenga suficiente memoria asignada (4GB mínimo recomendado)
