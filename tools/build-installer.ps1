$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
$jdk = 'C:\Program Files\Java\jdk-25.0.3'
$env:JAVA_HOME = $jdk
$buildVenv = Join-Path $root '.packaging\venv'
if (-not (Test-Path (Join-Path $buildVenv 'Scripts\pyinstaller.exe'))) {
    & python -m venv $buildVenv
    & (Join-Path $buildVenv 'Scripts\python.exe') -m pip install -r backend/requirements.txt pyinstaller pillow
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo preparar Python/PyInstaller.' }
}
& (Join-Path $buildVenv 'Scripts\python.exe') -c 'import PIL'
if ($LASTEXITCODE -ne 0) {
    & (Join-Path $buildVenv 'Scripts\python.exe') -m pip install pillow
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo instalar Pillow para generar los recursos visuales.' }
}
& (Join-Path $buildVenv 'Scripts\python.exe') tools/create-installer-art.py
if ($LASTEXITCODE -ne 0) { throw 'No se pudieron generar las imágenes de marca del instalador.' }
$dist = Join-Path $root '.packaging\backend-dist'
$work = Join-Path $root '.packaging\backend-work'
& (Join-Path $buildVenv 'Scripts\pyinstaller.exe') --noconfirm --clean --onedir --name CNNBBackend --distpath $dist --workpath $work --collect-all tensorflow --collect-all keras --add-data 'modelo/modelo_b_final.keras;modelo' backend/servidor_modelo.py
if ($LASTEXITCODE -ne 0) { throw 'Falló el empaquetado del backend TensorFlow.' }
& .\mvnw.cmd clean javafx:jlink
if ($LASTEXITCODE -ne 0) { throw 'Falló la imagen JavaFX/jlink.' }
$stage = Join-Path $root '.packaging\input'
if (Test-Path $stage) { Remove-Item -LiteralPath $stage -Recurse -Force }
New-Item -ItemType Directory -Force "$stage\backend", "$stage\modelo", "$stage\docs" | Out-Null
Copy-Item -Recurse -Force (Join-Path $dist 'CNNBBackend') "$stage\backend\CNNBBackend"
Copy-Item -Force backend/servidor_modelo.py "$stage\backend\servidor_modelo.py"
Copy-Item -Recurse -Force modelo/ecografias_prueba "$stage\modelo\ecografias_prueba"
Copy-Item -Force docs/metricas_test.csv "$stage\docs\metricas_test.csv"
$out = Join-Path $root 'instalador'
New-Item -ItemType Directory -Force $out | Out-Null
$image = Join-Path $root '.packaging\app-image-icon'
if (Test-Path $image) { Remove-Item -LiteralPath $image -Recurse -Force }
& (Join-Path $jdk 'bin\jpackage.exe') --type app-image --name CNN_B --app-version 1.0.0 --vendor 'Grupo 05 · UPN' --description 'Análisis local de ecografías mamarias' --icon tools/installer-assets/CNN_B.ico --input $stage --runtime-image (Join-Path $root 'target\app') --module-path (Join-Path $root 'target\app') --module com.grupo05.cnn_b/com.grupo05.cnn_b.HelloApplication --java-options '-Dcnn.root=$APPDIR' --java-options '-Duser.dir=$APPDIR' --dest $image --win-console
if ($LASTEXITCODE -ne 0) { throw 'Falló la creación de la imagen de aplicación.' }
$iscc = Join-Path $env:LOCALAPPDATA 'Programs\Inno Setup 6\ISCC.exe'
if (-not (Test-Path $iscc)) { throw 'Instala Inno Setup 6 para compilar el instalador (winget install JRSoftware.InnoSetup).' }
& $iscc /Q tools/CNN_B-Setup.iss
if ($LASTEXITCODE -ne 0) { throw 'Falló Inno Setup al crear el instalador EXE.' }
Write-Host "Instalador creado: $(Join-Path $out 'CNN_B-Instalador-1.0.0-x64.exe')"
