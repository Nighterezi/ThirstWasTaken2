$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location $projectRoot

try {
    & .\gradlew.bat clean build --no-daemon --stacktrace
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed with exit code $LASTEXITCODE"
    }

    Write-Host "Build complete. Release JARs:"
    Get-ChildItem build\libs\*.jar | Where-Object Name -NotLike "*-sources.jar" | ForEach-Object FullName
}
finally {
    Pop-Location
}
