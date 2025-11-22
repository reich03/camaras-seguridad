# Script para detener el sistema
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Security Camera System - Stop Script" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$projectPath = "d:\Trabajo\Proyecto miguel brayan\security-camera-system"
Set-Location $projectPath

Write-Host "Deteniendo servicios..." -ForegroundColor Yellow
docker-compose down

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✓ Servicios detenidos correctamente" -ForegroundColor Green
    Write-Host ""
    Write-Host "Para limpiar completamente (incluye volúmenes):" -ForegroundColor Yellow
    Write-Host "  docker-compose down -v" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "✗ Error al detener servicios" -ForegroundColor Red
}
