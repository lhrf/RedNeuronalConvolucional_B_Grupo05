# CNN_B · Clasificación de ecografías mamarias

Aplicación de escritorio para Windows que analiza imágenes de ecografía mamaria y estima a qué categoría pertenecen: **normal**, **benigna** o **maligna**. Fue desarrollada como proyecto académico del curso Sistemas Inteligentes y Machine Learning de la Universidad Privada del Norte (UPN).

> **Aviso importante.** Esta aplicación es un prototipo académico. **No es un dispositivo médico, no tiene validación clínica y no reemplaza el criterio de un especialista.** Sus resultados no deben usarse para diagnosticar ni para tomar decisiones de tratamiento. Cualquier hallazgo debe confirmarse con los procedimientos clínicos habituales (por ejemplo, la evaluación BI-RADS y la biopsia cuando corresponda).

---

## ¿Qué hace la aplicación?

1. El usuario abre una imagen de ecografía mamaria guardada en su computadora.
2. La aplicación analiza la imagen con un modelo de inteligencia artificial entrenado previamente.
3. Muestra la categoría más probable (normal, benigna o maligna) junto con el grado de confianza del modelo para cada una.

**Privacidad de los datos:** todo el análisis se realiza en la misma computadora. Las imágenes **no se envían a internet ni a ningún servidor externo**, y la aplicación funciona sin conexión.

## ¿Cómo funciona, en términos sencillos?

El programa utiliza una **red neuronal convolucional**, un tipo de inteligencia artificial especializado en reconocer patrones visuales. Se partió de un modelo llamado **EfficientNetB0**, que ya sabía reconocer formas y texturas generales en millones de fotografías. Después se le enseñó a distinguir ecografías mamarias mostrándole cientos de imágenes que especialistas ya habían clasificado. A esta versión final la llamamos **Modelo B**.

El modelo no "entiende" la anatomía como un médico: aprende asociaciones entre los patrones de la imagen (bordes, forma, ecogenicidad, textura) y la categoría que les corresponde. Por eso puede equivocarse con imágenes muy distintas a las que vio durante el entrenamiento, por ejemplo, de otro ecógrafo, con otra configuración o con anotaciones sobre la imagen.

## Datos con los que se entrenó y evaluó

Se usó el conjunto público **BUSI** (*Breast Ultrasound Images Dataset*; Al-Dhabyani et al., 2020), formado por 780 ecografías de mujeres de entre 25 y 75 años, obtenidas en el Hospital Baheya de El Cairo (Egipto) y clasificadas en normal, benigna y maligna.

Para medir el desempeño de forma honesta, se separó un grupo de **112 ecografías que el modelo nunca vio durante el entrenamiento** (conjunto de prueba).

## ¿Qué tan preciso es? Cómo interpretar los resultados

| Indicador | Resultado | Qué significa |
| --- | --- | --- |
| **Exactitud** | 76.79 % | De cada 100 ecografías, el modelo clasificó correctamente unas 77 y se equivocó en unas 23. |
| **Sensibilidad para casos malignos** | 87.10 % | De cada 100 lesiones malignas, el modelo identificó unas 87 como malignas. **Unas 13 de cada 100 no fueron detectadas**: un resultado "benigno" o "normal" no descarta malignidad. |
| **AUC** | 0.9266 | Mide la capacidad del modelo para distinguir entre categorías, en una escala de 0.5 (equivale al azar) a 1 (distinción perfecta). Un valor de 0.93 indica buena capacidad de discriminación. |
| **F1 macro** | 0.7635 | Promedio del equilibrio entre aciertos y errores en las tres categorías, dando el mismo peso a cada una. Va de 0 a 1; un valor de 0.76 indica un desempeño aceptable, pero mejorable. |

### Limitaciones que el especialista debe conocer

- **Muestra pequeña:** la evaluación se hizo con 112 imágenes de un solo hospital. Los resultados podrían ser distintos con pacientes, equipos o protocolos de otros centros.
- **Falsos negativos:** el modelo deja pasar aproximadamente 1 de cada 8 casos malignos.
- **Sin información clínica:** el modelo solo ve la imagen; no considera la edad, los antecedentes, el examen físico ni otras pruebas.
- **Calidad de la imagen:** las imágenes borrosas, recortadas o con marcas, mediciones o texto superpuesto pueden alterar el resultado.
- **Una imagen a la vez:** no analiza estudios completos ni compara con estudios anteriores.

## Cómo usar la aplicación

1. Ejecute el instalador (`.exe`) y siga los pasos en pantalla. No se necesita instalar ningún otro programa.
2. Abra **CNN_B** desde el menú Inicio o desde el acceso directo del escritorio. La primera carga puede tardar unos segundos mientras se prepara el modelo.
3. Seleccione la imagen de ecografía que desea analizar (formatos de imagen comunes, como PNG o JPG).
4. Revise el resultado: la categoría más probable y el porcentaje de confianza para cada una.

Para probar la aplicación sin usar imágenes propias, la carpeta `modelo/ecografias_prueba/` contiene ecografías de ejemplo.

**Recomendación de uso:** considere el resultado como una segunda opinión automatizada con fines educativos o de investigación, nunca como el diagnóstico final.

---

## Información técnica (para desarrolladores)

### Requisitos para ejecutar desde el código fuente

- Windows de 64 bits
- JDK 25
- Python 3.13

Desde PowerShell, en la carpeta raíz del proyecto:

```powershell
py -3.13 -m venv backend/.venv
backend/.venv/Scripts/python.exe -m pip install -r backend/requirements.txt
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25.0.3'
.\mvnw.cmd clean javafx:run
```

La interfaz está hecha con JavaFX. Al iniciar, un proceso local de Python (TensorFlow) carga una sola vez el modelo `modelo/modelo_b_final.keras` y atiende todas las predicciones en el mismo equipo.

### Crear el instalador de Windows

Instale JDK 25, Python 3.13 e Inno Setup 6. Luego, en PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File tools/build-installer.ps1
```

El instalador se genera en la carpeta `instalador/`. Incluye su propio entorno de Java, el modelo y el servicio de análisis con TensorFlow, por lo que el usuario final no necesita instalar nada más. La creación puede tardar varios minutos y requiere varios GB de espacio temporal en disco.

### Métricas y notebook

- `docs/metricas_test.csv`: resultados detallados de la evaluación con el conjunto de prueba.
- `docs/Notebook_Colab.ipynb`: notebook con el entrenamiento y la evaluación del modelo.

### Estructura del proyecto

| Carpeta | Contenido |
| --- | --- |
| `src/` | Aplicación JavaFX y pantallas (archivos FXML). |
| `backend/` | Servicio local de análisis en Python y sus dependencias. |
| `modelo/` | Modelo entrenado y ecografías de ejemplo. |
| `docs/` | Métricas de prueba y notebook de entrenamiento. |
| `tools/` | Herramientas de captura y script para crear el instalador. |

---

## Referencia

Al-Dhabyani, W., Gomaa, M., Khaled, H. y Fahmy, A. (2020). Dataset of breast ultrasound images. *Data in Brief, 28*, 104863. https://doi.org/10.1016/j.dib.2019.104863

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
