# CNN_B · Clasificación de ecografías mamarias

Aplicación de escritorio para Windows que analiza una ecografía mamaria y estima a cuál de tres categorías pertenece: **normal**, **benigna** o **maligna**. Es el prototipo del proyecto académico «Análisis comparativo de modelos CNN para el reconocimiento de lesiones mamarias en ecografías», desarrollado en el curso Sistemas Inteligentes y Machine Learning de la Universidad Privada del Norte (UPN).

> **Aviso importante.** CNN_B es un prototipo académico. **No es un dispositivo médico, no tiene validación clínica y no reemplaza el criterio de un especialista.** Sus resultados no deben usarse para diagnosticar ni para decidir un tratamiento. Todo hallazgo debe confirmarse con los procedimientos clínicos habituales, como la evaluación BI-RADS y la biopsia cuando corresponda.

---

## ¿Qué hace la aplicación?

1. El usuario carga una ecografía mamaria guardada en su computadora.
2. Un modelo de inteligencia artificial, entrenado previamente, analiza la imagen.
3. La aplicación muestra la categoría más probable, la probabilidad que el modelo asigna a cada categoría y el tiempo de análisis.
4. Si la probabilidad de la categoría elegida es menor que 60 %, la aplicación muestra el aviso «Confianza baja: se recomienda la revisión del especialista».

**Privacidad:** todo el análisis se realiza en la misma computadora. Las imágenes **no se envían a internet ni a ningún servidor externo**, y la aplicación funciona sin conexión.

## ¿Cómo funciona?

CNN_B utiliza una **red neuronal convolucional (CNN)**, un tipo de inteligencia artificial especializado en reconocer patrones visuales. El modelo, llamado **Modelo B**, parte de **EfficientNetB0**, una red que ya había aprendido a reconocer bordes, texturas y formas en más de un millón de fotografías generales (ImageNet). Sobre esa base, el modelo se adaptó a la ecografía mamaria con 522 imágenes que especialistas habían clasificado previamente. Durante el entrenamiento se aplicaron además variaciones aleatorias a las imágenes (volteo, rotación y zoom) y la técnica Dropout, para reducir el riesgo de que el modelo memorice los ejemplos.

El modelo no «entiende» la anatomía como un médico: aprende asociaciones entre los patrones de la imagen (bordes, forma, ecogenicidad y textura) y la categoría correspondiente. Por eso puede equivocarse con imágenes distintas a las del entrenamiento, por ejemplo, de otro ecógrafo, con otra configuración o con anotaciones sobre la imagen.

## Datos de entrenamiento y evaluación

Se usó el conjunto público **BUSI** (*Breast Ultrasound Images Dataset*; Al-Dhabyani et al., 2020): 780 ecografías de 600 mujeres de entre 25 y 75 años, obtenidas en el Hospital Baheya de El Cairo (Egipto) y clasificadas como normales, benignas o malignas.

Antes del entrenamiento se eliminaron 34 imágenes duplicadas o con etiquetas contradictorias. Las 746 ecografías restantes se dividieron en tres grupos, con la misma proporción de cada categoría:

| Grupo | Imágenes | Uso |
| --- | --- | --- |
| Entrenamiento | 522 | El modelo aprende de estas imágenes. |
| Validación | 112 | Sirven para controlar el entrenamiento. |
| Prueba | 112 | El modelo no las vio antes; miden su desempeño final. |

## ¿Qué tan preciso es?

Resultados del Modelo B en las 112 ecografías de prueba:

| Indicador | Resultado | Qué significa |
| --- | --- | --- |
| **Exactitud** | 76.79 % | El modelo clasificó correctamente 86 de las 112 ecografías. Con un grupo de prueba tan pequeño, el valor real podría estar entre 68.2 % y 83.6 % (intervalo de confianza del 95 %). |
| **Sensibilidad para casos malignos** | 87.10 % | Identificó como malignas 27 de las 31 lesiones malignas. **Las otras 4 no fueron detectadas**, por lo que un resultado «benigno» o «normal» no descarta malignidad. |
| **AUC macro** | 0.9266 | Mide la capacidad de distinguir cada categoría de las demás, en una escala de 0.5 (azar) a 1 (distinción perfecta). Un valor de 0.93 indica una buena capacidad de discriminación. |
| **F1 macro** | 0.7635 | Combina la precisión y la sensibilidad de las tres categorías, con el mismo peso para cada una. Va de 0 a 1; un valor de 0.76 indica un desempeño aceptable, pero mejorable. |

Como referencia, una CNN entrenada desde cero con los mismos datos (Modelo A) alcanzó una exactitud de 65.18 % y no detectó 12 de los 31 casos malignos. El Modelo B, que combina la transferencia de aprendizaje con el aumento de datos y Dropout, obtuvo mejores resultados en todas las métricas.

### Limitaciones que el especialista debe conocer

- **Un solo hospital y pocas imágenes de prueba:** los resultados podrían ser distintos con pacientes, equipos o protocolos de otros centros.
- **Falsos negativos:** en la prueba, el modelo no detectó 4 de cada 31 casos malignos (aproximadamente 1 de cada 8).
- **Posible sobrestimación:** BUSI no identifica a qué paciente pertenece cada imagen, por lo que la división en grupos se hizo por imagen y no por paciente. Si dos ecografías de una misma paciente quedaron en grupos distintos, las métricas podrían ser algo optimistas.
- **Las probabilidades no son certezas:** el porcentaje que muestra la aplicación no está calibrado. Un 90 % no significa que el modelo acierte 9 de cada 10 veces, y el umbral de 60 % del aviso de confianza baja se fijó de forma práctica, no clínica.
- **Acepta cualquier imagen:** la aplicación no verifica que la imagen sea una ecografía mamaria. Si recibe una mamografía o una fotografía, igual le asigna una categoría, y ese resultado no tiene validez.
- **Sin información clínica:** el modelo solo ve la imagen; no considera la edad, los antecedentes, el examen físico ni otras pruebas.
- **Calidad de la imagen:** las imágenes borrosas, recortadas o con marcas, mediciones o texto superpuesto pueden alterar el resultado.
- **Una imagen a la vez:** no analiza estudios completos ni compara con estudios anteriores.

## Cómo usar la aplicación

1. Ejecute el instalador (`.exe`) y siga los pasos en pantalla. No necesita instalar otros programas.
2. Abra **CNN_B** desde el menú Inicio o desde el acceso directo del escritorio. La primera carga puede tardar unos segundos mientras se prepara el modelo.
3. Entre al módulo de diagnóstico y arrastre una ecografía a la ventana o selecciónela desde el explorador de archivos (formatos PNG, JPG, JPEG o BMP).
4. Presione **Diagnosticar** y revise la categoría más probable y la probabilidad de cada categoría.
5. Si aparece el aviso de confianza baja, derive el caso a la revisión de un especialista.
6. Si necesita documentar el resultado, expórtelo como imagen PNG.

Para probar la aplicación sin imágenes propias, la carpeta `modelo/ecografias_prueba/` contiene ecografías de ejemplo del grupo de prueba.

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

- `docs/metricas_test.csv`: métricas del modelo de referencia y de los modelos A y B en el grupo de prueba.
- `docs/Notebook_Colab.ipynb`: copia del notebook `Proyecto_CNN_BUSI_v2.ipynb`, con la depuración de datos, el entrenamiento y la evaluación de ambos modelos.
- Notebook en Google Colab: https://colab.research.google.com/drive/1-V-ms5fIJBEhuZEeNmFVsqAGuTB6aNNG?usp=sharing

### Estructura del proyecto

| Carpeta | Contenido |
| --- | --- |
| `src/` | Aplicación JavaFX y pantallas (archivos FXML). |
| `backend/` | Servicio local de análisis en Python y sus dependencias. |
| `modelo/` | Modelo entrenado y ecografías de ejemplo. |
| `docs/` | Métricas de prueba y notebook de entrenamiento. |
| `tools/` | Herramientas de captura y script para crear el instalador. |

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
