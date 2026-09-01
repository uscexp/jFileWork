[CmdletBinding()]
param(
    [switch]$SkipMaven,
    [switch]$PrepareOnly,
    [ValidateSet('exe', 'app-image')]
    [string]$PackageType = 'exe',
    [switch]$AllowAppImageFallback,
    [string]$Configuration = 'Release'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Write-Step {
    param([string]$Message)
    Write-Host "[jFileWork Packaging] $Message"
}

function Test-WixToolsAvailable {
    $wixCommand = Get-Command wix -ErrorAction SilentlyContinue
    $candleCommand = Get-Command candle.exe -ErrorAction SilentlyContinue
    $lightCommand = Get-Command light.exe -ErrorAction SilentlyContinue
    return $null -ne $wixCommand -or ($null -ne $candleCommand -and $null -ne $lightCommand)
}

$root = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$uiModule = Join-Path $root 'ui-javafx'
$targetDir = Join-Path $uiModule 'target'
$packagingDir = Join-Path $targetDir 'packaging'
$appDir = Join-Path $packagingDir 'app'
$runtimeLibDir = Join-Path $packagingDir 'runtime-libs'
$runtimeDir = Join-Path $packagingDir 'runtime'
$installerDir = Join-Path $packagingDir 'installer'
$appName = 'jFileWork'
$appVersion = '0.0.1'
$mainClass = 'com.github.uscexp.jfilework.ui.javafx.MainLauncher'
$runtimeModules = @(
    'java.base',
    'java.desktop',
    'java.logging',
    'java.prefs',
    'java.xml',
    'jdk.unsupported',
    'javafx.controls',
    'javafx.fxml'
)

if (-not $env:JAVA_HOME) {
    throw 'JAVA_HOME is not set. Please point it to a JDK 26 installation.'
}

$jlinkExe = Join-Path $env:JAVA_HOME 'bin\jlink.exe'
$jpackageExe = Join-Path $env:JAVA_HOME 'bin\jpackage.exe'
if (-not (Test-Path $jlinkExe)) {
    throw "jlink.exe not found at $jlinkExe"
}
if (-not (Test-Path $jpackageExe)) {
    throw "jpackage.exe not found at $jpackageExe"
}

if ($SkipMaven) {
    Remove-Item -Recurse -Force -ErrorAction SilentlyContinue $appDir, $runtimeDir, $installerDir
} else {
    Remove-Item -Recurse -Force -ErrorAction SilentlyContinue $appDir, $runtimeLibDir, $runtimeDir, $installerDir
}

if (-not $SkipMaven) {
    Write-Step 'Running Maven package build...'
    & mvn -q -pl ui-javafx -am package
    if ($LASTEXITCODE -ne 0) { throw 'Maven package build failed.' }

    Write-Step 'Copying runtime dependencies into staging area...'
    & mvn -q -pl ui-javafx -am "-DskipTests=true" "-DoutputDirectory=$runtimeLibDir" "-DincludeScope=runtime" dependency:copy-dependencies
    if ($LASTEXITCODE -ne 0) { throw 'Dependency copy failed.' }
}

New-Item -ItemType Directory -Force -Path $appDir, $runtimeLibDir, $installerDir | Out-Null

$mainJar = Get-ChildItem -Path $targetDir -Filter 'jfilework-ui-javafx-*.jar' |
    Where-Object { $_.Name -notlike '*-sources*' -and $_.Name -notlike '*-javadoc*' } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if (-not $mainJar) {
    throw "Main application JAR not found in $targetDir"
}

Write-Step "Staging application JAR $($mainJar.Name) and dependencies..."
Copy-Item $mainJar.FullName $appDir -Force
Get-ChildItem -Path $runtimeLibDir -Filter '*.jar' | ForEach-Object {
    Copy-Item $_.FullName $appDir -Force
}

$javaFxJars = Get-ChildItem -Path $runtimeLibDir -Filter 'javafx-*.jar' |
    Sort-Object Name

if (-not $javaFxJars) {
    throw "No JavaFX runtime jars found in $runtimeLibDir. Run Maven with the windows-installer profile or copy dependencies first."
}

$modulePathEntries = @((Join-Path $env:JAVA_HOME 'jmods')) + ($javaFxJars.FullName)
$modulePath = ($modulePathEntries -join ';')
$addModules = ($runtimeModules -join ',')

Write-Step 'Building a custom runtime image with jlink...'
Write-Host "  module-path: $modulePath"
Write-Host "  add-modules: $addModules"
Write-Host "  output:      $runtimeDir"
if (-not $PrepareOnly) {
    & $jlinkExe `
        --module-path $modulePath `
        --add-modules $addModules `
        --output $runtimeDir `
        --strip-debug `
        --no-header-files `
        --no-man-pages `
        --compress=2
    if ($LASTEXITCODE -ne 0) { throw 'jlink failed.' }
}

Write-Step 'Preparing jpackage command...'
$effectivePackageType = $PackageType
if ($effectivePackageType -eq 'exe' -and -not (Test-WixToolsAvailable)) {
    if ($AllowAppImageFallback) {
        Write-Step 'WiX tools not found; falling back to app-image output.'
        $effectivePackageType = 'app-image'
    } else {
        throw 'WiX tools are required for exe packaging. Install WiX or rerun with -AllowAppImageFallback to create an app-image.'
    }
}

$packageArgs = @(
    '--type', $effectivePackageType,
    '--dest', $installerDir,
    '--name', $appName,
    '--app-version', $appVersion,
    '--input', $appDir,
    '--main-jar', $mainJar.Name,
    '--main-class', $mainClass,
    '--runtime-image', $runtimeDir
)

if ($effectivePackageType -eq 'exe') {
    $packageArgs += @('--win-dir-chooser', '--win-menu', '--win-shortcut')
}

Write-Host "  jpackage: $jpackageExe"
Write-Host "  args:     $($packageArgs -join ' ')"

if (-not $PrepareOnly) {
    & $jpackageExe @packageArgs
    if ($LASTEXITCODE -ne 0) { throw 'jpackage failed.' }
    Write-Step "Installer created in $installerDir"
} else {
    Write-Step 'Prepare-only mode: installer generation skipped.'
}






