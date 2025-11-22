# Script para iniciar el sistema completo
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Security Camera System - Startup Script" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Verificar Docker
Write-Host "Verificando Docker..." -ForegroundColor Yellow
$dockerVersion = docker --version 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Docker no está instalado o no está en el PATH" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Docker encontrado: $dockerVersion" -ForegroundColor Green
Write-Host ""

# Navegar al directorio
$projectPath = "d:\Trabajo\Proyecto miguel brayan\security-camera-system"
if (-not (Test-Path $projectPath)) {
    Write-Host "ERROR: No se encuentra el directorio del proyecto" -ForegroundColor Red
    exit 1
}
Set-Location $projectPath
Write-Host "✓ Directorio del proyecto: $projectPath" -ForegroundColor Green
Write-Host ""

# Detener servicios anteriores si existen
Write-Host "Deteniendo servicios anteriores..." -ForegroundColor Yellow
docker-compose down 2>$null
Write-Host ""

# Construir y levantar servicios
Write-Host "Construyendo y levantando servicios..." -ForegroundColor Yellow
Write-Host "Esto puede tardar varios minutos la primera vez..." -ForegroundColor Gray
docker-compose up -d --build

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Green
    Write-Host "✓ Sistema iniciado correctamente" -ForegroundColor Green
    Write-Host "============================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "URLs de acceso:" -ForegroundColor Cyan
    Write-Host "  • API Server:  http://localhost:8080" -ForegroundColor White
    Write-Host "  • Web Client:  http://localhost:8081" -ForegroundColor White
    Write-Host "  • MySQL:       localhost:3306" -ForegroundColor White
    Write-Host ""
    Write-Host "Usuarios de prueba:" -ForegroundColor Cyan
    Write-Host "  • admin / admin123" -ForegroundColor White
    Write-Host "  • user1 / admin123" -ForegroundColor White
    Write-Host "  • user2 / admin123" -ForegroundColor White
    Write-Host ""
    Write-Host "Comandos útiles:" -ForegroundColor Cyan
    Write-Host "  • Ver logs:     docker-compose logs -f" -ForegroundColor White
    Write-Host "  • Ver estado:   docker-compose ps" -ForegroundColor White
    Write-Host "  • Detener:      docker-compose down" -ForegroundColor White
    Write-Host ""
    
    # Esperar a que los servicios estén listos
    Write-Host "Esperando que los servicios estén listos..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
    
    # Abrir navegador
    Write-Host "Abriendo navegador..." -ForegroundColor Yellow
    Start-Process "http://localhost:8081"
} else {
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Red
    Write-Host "✗ Error al iniciar el sistema" -ForegroundColor Red
    Write-Host "============================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Ver logs con: docker-compose logs -f" -ForegroundColor Yellow
}
