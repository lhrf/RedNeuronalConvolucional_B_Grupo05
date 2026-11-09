"""Servidor local de inferencia de CNN_B mediante JSON Lines.

El proceso no abre puertos ni realiza conexiones de red.  Tras cargar el modelo
una vez, atiende una petición JSON por línea en stdin y escribe una respuesta
JSON por línea en stdout. Los diagnósticos y los registros de TensorFlow se
mantienen exclusivamente en stderr.
"""

from __future__ import annotations

import json
import os
import sys
import time
from pathlib import Path
from typing import Any

# Debe configurarse antes de importar TensorFlow para evitar registros en stdout.
os.environ.setdefault("TF_CPP_MIN_LOG_LEVEL", "2")

import tensorflow as tf


CLASES = ("benign", "malignant", "normal")
TAMANO_ENTRADA = (224, 224)
EXTENSIONES_ADMITIDAS = {".png", ".jpg", ".jpeg", ".bmp"}
RAIZ_PROYECTO = Path(__file__).resolve().parent.parent
RUTA_MODELO = (
    Path(getattr(sys, "_MEIPASS")) / "modelo" / "modelo_b_final.keras"
    if getattr(sys, "frozen", False)
    else RAIZ_PROYECTO / "modelo" / "modelo_b_final.keras"
)


def emitir(mensaje: dict[str, Any]) -> None:
    """Escribe exactamente un objeto JSON en stdout y lo vacía de inmediato."""
    print(json.dumps(mensaje, ensure_ascii=False, separators=(",", ":")), flush=True)


def preprocesar_imagen(ruta_imagen: Path) -> tf.Tensor:
    """Replica el preprocesamiento del notebook para una imagen local admitida.

    La división por 255 entrega datos en [0, 1]. El modelo contiene su propia
    capa Rescaling(255), por lo que aquí no se aplica ninguna escala adicional.
    """
    contenido = tf.io.read_file(str(ruta_imagen))
    imagen = tf.image.decode_image(contenido, channels=3, expand_animations=False)
    imagen.set_shape((None, None, 3))
    imagen = tf.image.resize(imagen, TAMANO_ENTRADA)
    return tf.cast(imagen, tf.float32) / 255.0


def validar_ruta(valor: Any) -> Path:
    if not isinstance(valor, str) or not valor.strip():
        raise ValueError("El campo 'path' debe ser una ruta de imagen no vacía.")

    ruta = Path(valor).expanduser().resolve()
    if ruta.suffix.lower() not in EXTENSIONES_ADMITIDAS:
        extensiones = ", ".join(sorted(EXTENSIONES_ADMITIDAS))
        raise ValueError(f"Formato no admitido. Use: {extensiones}.")
    if not ruta.is_file():
        raise FileNotFoundError("No se encontró el archivo de imagen indicado.")
    return ruta


def predecir(modelo: tf.keras.Model, ruta: Path) -> dict[str, Any]:
    inicio = time.perf_counter()
    imagen = preprocesar_imagen(ruta)
    probabilidades = modelo(tf.expand_dims(imagen, axis=0), training=False)[0].numpy()
    indice = int(tf.argmax(probabilidades).numpy())
    milisegundos = round((time.perf_counter() - inicio) * 1000)

    return {
        "ok": True,
        "clase": CLASES[indice],
        "probabilidades": {
            clase: round(float(probabilidades[posicion]), 6)
            for posicion, clase in enumerate(CLASES)
        },
        "ms": milisegundos,
    }


def atender(modelo: tf.keras.Model) -> None:
    for linea in sys.stdin:
        try:
            solicitud = json.loads(linea)
            if not isinstance(solicitud, dict):
                raise ValueError("La solicitud debe ser un objeto JSON.")

            comando = solicitud.get("cmd")
            if comando == "predict":
                emitir(predecir(modelo, validar_ruta(solicitud.get("path"))))
            elif comando == "shutdown":
                emitir({"ok": True, "shutdown": True})
                return
            else:
                raise ValueError("Comando no admitido. Use 'predict' o 'shutdown'.")
        except Exception as error:  # El contrato del proceso comunica el error al cliente Java.
            print(f"[CNN_B] {type(error).__name__}: {error}", file=sys.stderr, flush=True)
            emitir({"ok": False, "error": str(error)})


def main() -> int:
    sys.stdout.reconfigure(encoding="utf-8", line_buffering=True)
    sys.stderr.reconfigure(encoding="utf-8", line_buffering=True)
    try:
        if not RUTA_MODELO.is_file():
            raise FileNotFoundError(f"No se encontró el modelo en: {RUTA_MODELO}")
        modelo = tf.keras.models.load_model(RUTA_MODELO, compile=False)
        emitir({"ready": True})
        atender(modelo)
        return 0
    except Exception as error:
        print(f"[CNN_B] No fue posible iniciar el modelo: {error}", file=sys.stderr, flush=True)
        emitir({"ready": False, "error": str(error)})
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
