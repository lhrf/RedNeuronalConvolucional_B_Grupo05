$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$OutputEncoding = [Console]::OutputEncoding
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectRoot
$jdk = 'C:\Program Files\Java\jdk-25.0.3'
$env:JAVA_HOME = $jdk
& .\mvnw.cmd clean compile
if ($LASTEXITCODE -ne 0) { throw 'Falló la compilación Maven.' }
[xml]$pom = Get-Content -LiteralPath pom.xml
$fxVersion = $pom.project.properties.'javafx.version'
$mavenRepository = Join-Path $env:USERPROFILE '.m2/repository'
$fxJars = @('base','graphics','controls','fxml','swing') | ForEach-Object {
    Join-Path $mavenRepository "org/openjfx/javafx-$_/$fxVersion/javafx-$_-$fxVersion-win.jar"
}
$gson = Join-Path $mavenRepository 'com/google/code/gson/gson/2.13.2/gson-2.13.2.jar'
$modulePath = $fxJars -join ';'
$classPath = "target/classes;tools/.classes;$gson"
New-Item -ItemType Directory -Path tools/.classes -Force | Out-Null
& "$jdk/bin/javac.exe" -encoding UTF-8 --module-path $modulePath --add-modules javafx.controls,javafx.fxml,javafx.swing -cp $classPath -d tools/.classes tools/CaptureLauncher.java
if ($LASTEXITCODE -ne 0) { throw 'Falló la compilación de la herramienta de capturas.' }
& "$jdk/bin/java.exe" '-Dstdout.encoding=UTF-8' '-Dstderr.encoding=UTF-8' --enable-native-access=javafx.graphics --module-path $modulePath --add-modules javafx.controls,javafx.fxml,javafx.swing -cp $classPath com.grupo05.cnn_b.CaptureLauncher
if ($LASTEXITCODE -ne 0) { throw 'Falló la verificación visual.' }
