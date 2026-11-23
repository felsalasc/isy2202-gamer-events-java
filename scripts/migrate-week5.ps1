$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $taskRoot
try {
    foreach ($taskFile in @('db/migrations/2026-11-23-owner.sql','db/03-interactions.sql','db/04-community.sql','db/05-demo-owners.sql')) {
        Get-Content -LiteralPath $taskFile -Raw -Encoding utf8 |
            docker compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysql --default-character-set=utf8mb4 -u "$MYSQL_USER" "$MYSQL_DATABASE"'
        if ($LASTEXITCODE -ne 0) { throw "Falló la migración $taskFile. No inicies la aplicación nueva." }
    }
    Write-Output 'Esquema de semana 5 actualizado; se conservaron los datos existentes.'
} finally { Pop-Location }
