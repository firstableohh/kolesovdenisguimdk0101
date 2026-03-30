# Запуск фитнес-приложения (JavaFX + Maven).
# Дважды щелкните или в PowerShell:  .\run-fitness.ps1
# Если политика запрещает скрипты:  Set-ExecutionPolicy -Scope CurrentUser RemoteSigned

try {
    [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
    $OutputEncoding = [System.Text.Encoding]::UTF8
} catch {}

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

# 1) Java: сначала JAVA_HOME, иначе ищем Temurin в Program Files
if (-not $env:JAVA_HOME -or -not (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $candidates = @(
        "C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot",
        "C:\Program Files\Eclipse Adoptium\jdk-17.0.13.11-hotspot",
        "C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot"
    )
    foreach ($d in $candidates) {
        if (Test-Path "$d\bin\java.exe") {
            $env:JAVA_HOME = $d
            break
        }
    }
    if (-not $env:JAVA_HOME) {
        $adoptium = Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -match "^jdk-(17|21)" } | Sort-Object Name -Descending | Select-Object -First 1
        if ($adoptium) { $env:JAVA_HOME = $adoptium.FullName }
    }
}
if (-not $env:JAVA_HOME -or -not (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    Write-Host "Не найден JDK 17+. Установите Temurin: winget install EclipseAdoptium.Temurin.21.JDK" -ForegroundColor Red
    exit 1
}

# 2) Maven: локальная копия в .devtools (уже скачана в проект) или системный mvn
$mvnBin = Join-Path $ProjectRoot ".devtools\apache-maven-3.9.14\bin"
if (Test-Path "$mvnBin\mvn.cmd") {
    $env:PATH = "$mvnBin;$env:JAVA_HOME\bin;$env:PATH"
} else {
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
    if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
        Write-Host "Maven не найден. В репозитории должен быть каталог .devtools\apache-maven-3.9.14 (выполните git pull) или установите Maven: winget install Apache.Maven" -ForegroundColor Red
        exit 1
    }
}

Set-Location $ProjectRoot
Write-Host "JAVA_HOME=$env:JAVA_HOME" -ForegroundColor DarkGray
& mvn -version
Write-Host "`nЗапуск приложения...`n" -ForegroundColor Cyan
& mvn javafx:run
