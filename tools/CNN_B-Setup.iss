#define AppName "CNN_B"
#define AppVersion "1.0.0"
#define AppPublisher "Grupo 05 · Universidad Privada del Norte"
#define AppExeName "CNN_B.exe"

[Setup]
AppId={{8F2CA266-E1B7-4BEA-8C09-9418566B5EF0}
AppName={#AppName}
AppVersion={#AppVersion}
AppPublisher={#AppPublisher}
DefaultDirName={autopf}\CNN_B
DefaultGroupName=CNN_B
OutputDir=..\instalador
OutputBaseFilename=CNN_B-Instalador-1.0.0-x64
ArchitecturesInstallIn64BitMode=x64
PrivilegesRequired=admin
Compression=none
SolidCompression=no
WizardStyle=modern
SetupIconFile=installer-assets\CNN_B.ico
WizardImageFile=installer-assets\wizard.bmp
WizardSmallImageFile=installer-assets\wizard-small.bmp
UninstallDisplayIcon={app}\{#AppExeName}
CloseApplications=yes
RestartApplications=no

[Languages]
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"

[Tasks]
Name: "desktopicon"; Description: "Crear un acceso directo en el escritorio"; GroupDescription: "Accesos directos:"; Flags: unchecked

[Files]
Source: "..\.packaging\app-image-icon\CNN_B\*"; DestDir: "{app}"; Excludes: "*\tensorflow\include\*"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\CNN_B"; Filename: "{app}\{#AppExeName}"
Name: "{autodesktop}\CNN_B"; Filename: "{app}\{#AppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#AppExeName}"; Description: "Iniciar CNN_B"; Flags: postinstall nowait skipifsilent
