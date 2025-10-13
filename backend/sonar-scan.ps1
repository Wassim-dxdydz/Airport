# Load SONAR_TOKEN from .env file
$envFile = ".env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if ($_ -match "^\s*SONAR_TOKEN\s*=") {
            $parts = $_.Split("=")
            $env:SONAR_TOKEN = $parts[1].Trim()
        }
    }
}

if (-not $env:SONAR_TOKEN) {
    Write-Host "❌ ERROR: SONAR_TOKEN not found in .env file" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Running Maven Sonar analysis..." -ForegroundColor Cyan
./mvnw clean verify sonar:sonar "-Dsonar.login=$env:SONAR_TOKEN"
