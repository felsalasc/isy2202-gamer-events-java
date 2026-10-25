param([ValidateRange(0, 65535)][int]$Port = 0)
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $taskRoot
try {
    $taskLocalJdk = Get-ChildItem -LiteralPath '.tools/java17' -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($taskLocalJdk) { $env:JAVA_HOME = $taskLocalJdk.FullName }
    $taskJava = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/java.exe' } else { 'java' }
    New-Item -ItemType Directory -Path '.tools' -Force | Out-Null
    if (-not $env:GAMER_DEMO_PASSWORD) {
        if (-not (Test-Path -LiteralPath '.tools/demo-password.txt')) {
            $taskBytes = New-Object byte[] 24
            $taskRng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
            try { $taskRng.GetBytes($taskBytes) } finally { $taskRng.Dispose() }
            [Convert]::ToBase64String($taskBytes) | Set-Content -LiteralPath '.tools/demo-password.txt' -Encoding ascii
        }
        $env:GAMER_DEMO_PASSWORD = (Get-Content -LiteralPath '.tools/demo-password.txt' -Raw).Trim()
        Write-Host 'Contraseña de demostración en .tools/demo-password.txt (excluida de Git y del ZIP).'
    }
    Write-Host 'Usuarios: gamer1@example.test, gamer2@example.test, gamer3@example.test'
    Write-Host 'El puerto se mostrará en la línea Tomcat started on port del log.'
    & $taskJava -jar 'target/gamer-events-0.0.1-SNAPSHOT.jar' '--spring.profiles.active=demo' "--server.port=$Port"
    if ($LASTEXITCODE -ne 0) { throw 'La aplicación no pudo iniciar. Revisa el log anterior.' }
} finally { Pop-Location }
