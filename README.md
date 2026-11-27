# CNN_B · Clasificación de ecografías mamarias

Aplicación de escritorio para Windows que analiza una ecografía mamaria y estima a cuál de tres categorías pertenece: **benigna**, **maligna** o **normal**. Es el prototipo del proyecto académico «Análisis comparativo de modelos CNN para el reconocimiento de lesiones mamarias en ecografías», desarrollado por el Grupo 05 en el curso Sistemas Inteligentes y Machine Learning de la Universidad Privada del Norte (UPN).

> **Aviso importante.** CNN_B es un prototipo académico. **No es un dispositivo médico, no tiene validación clínica y no reemplaza el criterio de un especialista.** Sus resultados no deben usarse para diagnosticar ni para decidir un tratamiento. Todo hallazgo debe confirmarse con los procedimientos clínicos habituales, como la evaluación BI-RADS y la biopsia cuando corresponda.

---

## Descarga e instalación

1. Descargue el instalador de Windows (`.exe`) desde la sección **[Releases](https://github.com/lhrf/RedNeuronalConvolucional_B_Grupo05/releases)** de este repositorio (versión **CNN_B Setup v1.0.0**).
2. Ejecute el instalador. Requiere Windows de 64 bits y permisos de administrador.
3. Siga los pasos del asistente, que está en español. Puede marcar la opción «Crear un acceso directo en el escritorio».
4. Al finalizar, el asistente ofrece iniciar CNN_B. Después puede abrirla desde el menú Inicio.

El instalador incluye todo lo necesario: su propio entorno de Java, el modelo entrenado, el servicio de análisis y tres ecografías de ejemplo. No se necesita instalar otros programas ni tener conexión a internet.

**Privacidad:** todo el análisis se realiza en la misma computadora. Las imágenes **no se envían a internet ni a ningún servidor externo**.

## Módulos de la aplicación

| Módulo | Qué muestra |
| --- | --- |
| **Diagnóstico** | Carga una ecografía, ejecuta el modelo y muestra la clase predicha, la probabilidad de cada clase y el tiempo de inferencia. |
| **Cómo analiza la red** | Una animación que resume, en cinco etapas, el recorrido de la imagen dentro del modelo: imagen, detección de rasgos, resumen, combinación y resultado. |
| **Métricas del modelo** | El desempeño del Modelo B en las 112 ecografías de prueba: exactitud, F1 macro, AUC, sensibilidad maligna y matriz de confusión. |
| **Acerca del proyecto** | Objetivo, conjunto de datos, método, limitaciones y créditos del Grupo 05. |

## Cómo usar la aplicación

1. Abra **CNN_B**. La primera carga tarda unos segundos mientras se prepara el modelo («Cargando modelo…»).
2. En el módulo **Diagnóstico**, cargue una ecografía de una de estas formas:
   - arrástrela a la ventana;
   - presione **Seleccionar archivo** (formatos PNG, JPG, JPEG o BMP);
   - presione **Usar imagen de ejemplo** para probar con las ecografías incluidas.
3. Presione **Diagnosticar** y revise la clase predicha y la probabilidad de cada clase.
4. Si la probabilidad de la clase predicha es menor que 60 %, la aplicación muestra el aviso **«Confianza baja: se recomienda la revisión del especialista»**. En ese caso, derive el caso a un especialista.
5. Para documentar el resultado, presione **Exportar resultado** y guárdelo como imagen PNG.
6. Para analizar otra imagen, presione **Nuevo análisis** o **Quitar imagen**.

**Recomendación de uso:** considere el resultado como una segunda opinión automatizada con fines educativos o de investigación, nunca como el diagnóstico final.

## ¿Cómo funciona?

CNN_B utiliza una **red neuronal convolucional (CNN)**, un tipo de inteligencia artificial especializado en reconocer patrones visuales. El modelo, llamado **Modelo B**, parte de **EfficientNetB0**, una red que ya había aprendido a reconocer bordes, texturas y formas en más de un millón de fotografías generales (ImageNet). Sobre esa base se agregaron una capa de resumen (Global Average Pooling), una capa densa de 256 neuronas y una salida de tres clases, y el modelo se adaptó a la ecografía mamaria con 522 imágenes clasificadas previamente por especialistas. Durante el entrenamiento se aplicaron variaciones aleatorias a las imágenes (volteo, rotación y zoom) y la técnica Dropout, para reducir el riesgo de que el modelo memorice los ejemplos.

El modelo no «entiende» la anatomía como un médico: aprende asociaciones entre los patrones de la imagen (bordes, forma, ecogenicidad y textura) y la categoría correspondiente. Por eso puede equivocarse con imágenes distintas a las del entrenamiento, por ejemplo, de otro ecógrafo, con otra configuración o con anotaciones sobre la imagen.

## Datos de entrenamiento y evaluación

Se usó el conjunto público **BUSI** (*Breast Ultrasound Images Dataset*; Al-Dhabyani et al., 2020): 780 ecografías de 600 mujeres de entre 25 y 75 años, adquiridas en 2018 en el Hospital Baheya de El Cairo (Egipto): 437 benignas, 210 malignas y 133 normales.

Antes del entrenamiento se eliminaron 34 imágenes duplicadas o con etiquetas contradictorias. Las 746 ecografías restantes se dividieron en tres grupos, con la misma proporción de cada categoría:

| Grupo | Imágenes | Uso |
| --- | --- | --- |
| Entrenamiento | 522 | El modelo aprende de estas imágenes. |
| Validación | 112 | Sirven para controlar el entrenamiento. |
| Prueba | 112 | El modelo no las vio antes; miden su desempeño final. |

## ¿Qué tan preciso es?

Resultados del Modelo B en las 112 ecografías de prueba (los mismos que muestra el módulo **Métricas del modelo**):

| Indicador | Resultado | Qué significa |
| --- | --- | --- |
| **Exactitud** | 76.79 % | El modelo clasificó correctamente 86 de las 112 ecografías. Con un grupo de prueba tan pequeño, el valor real podría estar entre 68.2 % y 83.6 % (intervalo de confianza del 95 %). |
| **Sensibilidad para casos malignos** | 87.10 % | Identificó como malignas 27 de las 31 lesiones malignas. **Las otras 4 no fueron detectadas**, por lo que un resultado «benigno» o «normal» no descarta malignidad. |
| **AUC macro** | 0.9266 | Mide la capacidad de distinguir cada categoría de las demás, en una escala de 0.5 (azar) a 1 (distinción perfecta). Un valor de 0.93 indica una buena capacidad de discriminación. |
| **F1 macro** | 0.7635 | Combina la precisión y la sensibilidad de las tres categorías, con el mismo peso para cada una. Va de 0 a 1; un valor de 0.76 indica un desempeño aceptable, pero mejorable. |

En el proyecto, el Modelo B se comparó con una CNN entrenada desde cero con los mismos datos (Modelo A) y con un modelo de referencia que siempre predice la clase mayoritaria. El Modelo B, que combina la transferencia de aprendizaje con el aumento de datos y Dropout, superó a ambos en todas las métricas. Las cifras de los tres modelos están en `docs/metricas_test.csv` y en el notebook `docs/Notebook_Colab.ipynb`.

### Limitaciones que el especialista debe conocer

- **Un solo hospital y pocas imágenes de prueba:** los resultados podrían ser distintos con pacientes, equipos o protocolos de otros centros.
- **Falsos negativos:** en la prueba, el modelo no detectó 4 de 31 casos malignos (aproximadamente 1 de cada 8).
- **Posible sobrestimación:** BUSI no identifica a qué paciente pertenece cada imagen, por lo que la división en grupos se hizo por imagen y no por paciente. Si dos ecografías de una misma paciente quedaron en grupos distintos, las métricas podrían ser algo optimistas.
- **Las probabilidades no son certezas:** el porcentaje que muestra la aplicación no está calibrado. Un 90 % no significa que el modelo acierte 9 de cada 10 veces, y el umbral de 60 % del aviso de confianza baja se fijó de forma práctica, no clínica.
- **Acepta cualquier imagen:** la aplicación no verifica que la imagen sea una ecografía mamaria. Si recibe una mamografía o una fotografía, igual le asigna una categoría, y ese resultado no tiene validez.
- **Sin información clínica:** el modelo solo ve la imagen; no considera la edad, los antecedentes, el examen físico ni otras pruebas.
- **Calidad de la imagen:** las imágenes borrosas, recortadas o con marcas, mediciones o texto superpuesto pueden alterar el resultado.
- **Una imagen a la vez:** no analiza estudios completos ni compara con estudios anteriores.

---

## Información técnica (para desarrolladores)

### Arquitectura de la aplicación

- **Interfaz:** JavaFX 27 (Java 25), con pantallas definidas en archivos FXML.
- **Inferencia:** al iniciar, la aplicación lanza un proceso local de Python (`backend/servidor_modelo.py`) que carga una sola vez `modelo/modelo_b_final.keras` con TensorFlow y responde cada predicción mediante mensajes JSON por la entrada y la salida estándar. No se abre ningún puerto de red.
- **Métricas:** el módulo de métricas lee la fila del Modelo B en `docs/metricas_test.csv`.

### Ejecutar desde el código fuente

Requisitos: Windows de 64 bits, JDK 25 y Python 3.13. Desde PowerShell, en la carpeta raíz del proyecto:

```powershell
py -3.13 -m venv backend/.venv
backend/.venv/Scripts/python.exe -m pip install -r backend/requirements.txt
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25.0.3'
.\mvnw.cmd clean javafx:run
```

`backend/requirements.txt` fija TensorFlow 2.21.0 y Keras 3.13.2, la versión con la que se guardó el modelo.

Para comprobar el modelo sin abrir la interfaz, ejecute el script de prueba, que clasifica las tres ecografías de ejemplo e imprime sus probabilidades:

```powershell
backend/.venv/Scripts/python.exe backend/probar_modelo.py
```

### Crear el instalador de Windows

Instale JDK 25, Python 3.13 e Inno Setup 6 (`winget install JRSoftware.InnoSetup`). Luego, en PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File tools/build-installer.ps1
```

El script empaqueta el backend de TensorFlow con PyInstaller, crea la imagen de la aplicación con jlink y jpackage, y compila el instalador con Inno Setup (`tools/CNN_B-Setup.iss`). El resultado, `CNN_B-Instalador-1.0.0-x64.exe`, se guarda en la carpeta `instalador/`. La creación puede tardar varios minutos y requiere varios GB de espacio temporal en disco.

### Estructura del proyecto

| Ruta | Contenido |
| --- | --- |
| `src/` | Aplicación JavaFX: controladores Java, pantallas FXML y estilos CSS. |
| `backend/` | Servicio local de inferencia (`servidor_modelo.py`), script de prueba (`probar_modelo.py`) y dependencias. |
| `modelo/` | Modelo entrenado (`modelo_b_final.keras`) y tres ecografías de ejemplo (benigna, maligna y normal) del grupo de prueba. |
| `docs/` | Métricas de prueba (`metricas_test.csv`) y notebook de entrenamiento y evaluación (`Notebook_Colab.ipynb`). |
| `tools/` | Script y configuración del instalador, recursos gráficos del asistente y una herramienta para generar capturas de pantalla de la aplicación. |

El notebook también está disponible en Google Colab: https://colab.research.google.com/drive/1-V-ms5fIJBEhuZEeNmFVsqAGuTB6aNNG?usp=sharing

---

## Referencias

Al-Dhabyani, W., Gomaa, M., Khaled, H., y Fahmy, A. (2020). Dataset of breast ultrasound images. *Data in Brief, 28*, 104863. https://doi.org/10.1016/j.dib.2019.104863

Tan, M., y Le, Q. V. (2019). EfficientNet: Rethinking model scaling for convolutional neural networks. En *Proceedings of the 36th International Conference on Machine Learning* (Vol. 97, pp. 6105–6114). PMLR.

## Créditos

**Grupo 05** · Universidad Privada del Norte (UPN), Perú
Curso: Sistemas Inteligentes y Machine Learning
Docente: Marcelino Torres Villanueva

Integrantes:

- Cano Sanchez Carlos Fabrizio Benjamin
- Kong Morales Fernando
- Rodriguez Flores Luis Hernan
- Sanchez Briceño Gerson Aldair Vicente
- Silva Diaz Cristian Jesús
