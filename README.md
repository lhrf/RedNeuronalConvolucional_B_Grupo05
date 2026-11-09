# CNN_B · Clasificación de ecografías mamarias

Aplicación de escritorio JavaFX para analizar ecografías mamarias localmente con el Modelo B, basado en EfficientNetB0. La inferencia se ejecuta en un proceso Python local y no requiere conexión de red durante el uso.

## Requisitos para ejecutar desde el código

- Windows x64
- JDK 25
- Python 3.13

Desde PowerShell, en la raíz del proyecto:

```powershell
py -3.13 -m venv backend/.venv
backend/.venv/Scripts/python.exe -m pip install -r backend/requirements.txt
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25.0.3'
.\mvnw.cmd clean javafx:run
```

El backend carga `modelo/modelo_b_final.keras` una vez al inicio y atiende las predicciones en el equipo. Las ecografías de `modelo/ecografias_prueba/` sirven como ejemplos.

## Métricas de evaluación

Resultados del conjunto de prueba BUSI (112 ecografías):

- Exactitud: 76.79 %
- F1 macro: 0.7635
- AUC: 0.9266
- Sensibilidad para la clase maligna: 87.10 %

Los datos de evaluación están en `docs/metricas_test.csv`. El notebook del proyecto se incluye en `docs/Notebook_Colab.ipynb`.

## Crear el instalador de Windows

Instala JDK 25, Python 3.13 e Inno Setup 6. En PowerShell, ejecuta:

```powershell
powershell -ExecutionPolicy Bypass -File tools/build-installer.ps1
```

El instalador generado aparece en `instalador/`. El empaquetado incluye un runtime Java, el modelo y un backend TensorFlow autónomo. La creación puede tardar y requiere varios GB de espacio temporal.

## Estructura

- `src/`: aplicación JavaFX y pantallas FXML.
- `backend/`: servicio local de inferencia y dependencias Python.
- `modelo/`: modelo entrenado y ecografías de ejemplo.
- `docs/`: métricas de prueba y notebook.
- `tools/`: herramientas de captura y build del instalador.

## Créditos

Grupo 05 · Universidad Privada del Norte (UPN), Perú. Curso: Sistemas Inteligentes y Machine Learning. Docente: Marcelino Torres Villanueva.

- Cano Sanchez Carlos Fabrizio Benjamin
- Kong Morales Fernando
- Rodriguez Flores Luis Hernan
- Sanchez Briceño Gerson Aldair Vicente
- Silva Diaz Cristian Jesús
