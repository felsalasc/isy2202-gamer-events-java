# Levanta Gamer Events (aplicación + MySQL) con Docker Compose y espera a que responda.
# Requisitos: Docker Desktop en ejecución y un archivo .env completo (copia de .env.example).
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $projectRoot
try {
    if (-not (Test-Path -LiteralPath '.env')) {
        throw 'Falta el archivo .env. Cópialo desde .env.example (Copy-Item .env.example .env) y completa las contraseñas.'
    }

    docker info *> $null
    if ($LASTEXITCODE -ne 0) {
        throw 'Docker no está en ejecución. Abre Docker Desktop e inténtalo otra vez.'
    }

    docker compose up --build -d
    if ($LASTEXITCODE -ne 0) { throw 'docker compose no pudo levantar los servicios. Revisa el mensaje anterior.' }

    Write-Host 'Esperando a que la aplicación responda...'
    for ($attempt = 0; $attempt -lt 60; $attempt++) {
        try {
            Invoke-WebRequest -Uri 'http://localhost/api/home' -UseBasicParsing -TimeoutSec 3 | Out-Null
            Write-Host ''
            Write-Host 'Gamer Events está listo en http://localhost'
            Write-Host 'Usuarios y contraseñas de prueba: ver el informe de entrega'
            Write-Host 'Ver el log: docker compose logs -f gamer-events   |   Detener: docker compose down'
            exit 0
        } catch {
            Start-Sleep -Seconds 3
        }
    }
    throw 'La aplicación no respondió a tiempo. Revisa el log con: docker compose logs gamer-events'
} finally {
    Pop-Location
}
