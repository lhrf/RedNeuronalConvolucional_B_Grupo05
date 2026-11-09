# Capturas del paso 1

Desde la raíz, ejecutar `powershell -ExecutionPolicy Bypass -File tools/capture-step1.ps1`.

La herramienta compila la app y un lanzador independiente, inicia el backend real y guarda `Scene.snapshot()` en `diseno/verificacion/`. No se compila ni se empaqueta con la aplicación Maven. `UiStyles` permanece en `src/main` porque aplica estilos en la app real.

La captura maligna usa `malignant_malignant (115).png` y su inferencia real. Solo la captura de baja confianza y su variante compacta usan un JSON simulado (48 %, 37 %, 15 % y 123 ms). Ese JSON existe exclusivamente en `tools/CaptureLauncher.java`; no modifica el modelo, el backend ni los resultados de la aplicación.

Las capturas usan escenas de la ventana JavaFX activa, con el tamaño del área de contenido indicado en el nombre. El lanzador ajusta el Stage por la diferencia real de decoración nativa antes de `Scene.snapshot()`. La escena de 1024×680 verifica el contenido desbordado y la barra de desplazamiento.
