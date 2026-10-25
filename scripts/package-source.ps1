$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$taskRoot = [System.IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$taskDist = Join-Path $taskRoot 'dist'
New-Item -ItemType Directory -Path $taskDist -Force | Out-Null
$taskArchivePath = Join-Path $taskDist 'gamer-events-semana1-fuente.zip'
$taskStream = [System.IO.File]::Open($taskArchivePath, [System.IO.FileMode]::Create)
$taskArchive = [System.IO.Compression.ZipArchive]::new($taskStream, [System.IO.Compression.ZipArchiveMode]::Create)
try {
    $taskFiles = @('pom.xml', 'mvnw', 'mvnw.cmd', '.gitignore', '.gitattributes', 'README.md') |
        ForEach-Object { Get-Item -LiteralPath (Join-Path $taskRoot $_) }
    foreach ($taskDirectory in @('src', 'docs', '.mvn', 'scripts')) {
        $taskFiles += Get-ChildItem -LiteralPath (Join-Path $taskRoot $taskDirectory) -File -Recurse
    }
    foreach ($taskFile in $taskFiles) {
        $taskRelativePath = $taskFile.FullName.Substring($taskRoot.Length + 1).Replace('\', '/')
        [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($taskArchive, $taskFile.FullName, $taskRelativePath) | Out-Null
    }
} finally {
    $taskArchive.Dispose()
    $taskStream.Dispose()
}
Write-Output $taskArchivePath
