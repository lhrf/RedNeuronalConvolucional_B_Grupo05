"""Prueba el protocolo JSON Lines con las tres ecografías incluidas en el proyecto."""

from __future__ import annotations

import json
import subprocess
import sys
from pathlib import Path


RAIZ_PROYECTO = Path(__file__).resolve().parent.parent
CARPETA_PRUEBAS = RAIZ_PROYECTO / "modelo" / "ecografias_prueba"
SERVIDOR = Path(__file__).resolve().parent / "servidor_modelo.py"
CLASES = ("benign", "malignant", "normal")


def recibir(proceso: subprocess.Popen[str]) -> dict:
    linea = proceso.stdout.readline()
    if not linea:
        detalle = proceso.stderr.read().strip()
        raise RuntimeError(f"El servidor terminó antes de responder. {detalle}")
    return json.loads(linea)


def enviar(proceso: subprocess.Popen[str], mensaje: dict) -> dict:
    assert proceso.stdin is not None
    proceso.stdin.write(json.dumps(mensaje) + "\n")
    proceso.stdin.flush()
    return recibir(proceso)


def main() -> int:
    proceso = subprocess.Popen(
        [sys.executable, str(SERVIDOR)],
        cwd=RAIZ_PROYECTO,
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        encoding="utf-8",
        bufsize=1,
    )

    try:
        inicio = recibir(proceso)
        if inicio.get("ready") is not True:
            raise RuntimeError(inicio.get("error", "El servidor no quedó listo."))

        print("Modelo cargado. Resultados de las ecografías de prueba:\n")
        for clase_esperada in CLASES:
            imagen = next(CARPETA_PRUEBAS.glob(f"{clase_esperada}_*"), None)
            if imagen is None:
                raise FileNotFoundError(f"No hay imagen de prueba para la clase {clase_esperada}.")
            respuesta = enviar(proceso, {"cmd": "predict", "path": str(imagen.resolve())})
            if not respuesta.get("ok"):
                raise RuntimeError(respuesta.get("error", "Error de inferencia."))

            probabilidades = respuesta["probabilidades"]
            texto_probabilidades = ", ".join(
                f"{clase}={probabilidades[clase]:.6f}" for clase in CLASES
            )
            print(
                f"{imagen.name}\n"
                f"  predicción={respuesta['clase']} | {texto_probabilidades}"
                f" | {respuesta['ms']} ms\n"
            )
        return 0
    finally:
        if proceso.poll() is None:
            try:
                enviar(proceso, {"cmd": "shutdown"})
            except (BrokenPipeError, RuntimeError, json.JSONDecodeError):
                proceso.terminate()
        proceso.wait(timeout=10)
        if proceso.stderr is not None:
            registros = proceso.stderr.read().strip()
            if registros:
                print(registros, file=sys.stderr)


if __name__ == "__main__":
    raise SystemExit(main())
