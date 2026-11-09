package com.grupo05.cnn_b;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Consulta metricas_test.csv en ejecución; las cifras no se duplican en Java. */
public final class MetricasCsv {
    private MetricasCsv() {}
    public static Map<String, String> modeloB(Path root) throws IOException {
        List<String> lineas = Files.readAllLines(root.resolve("docs/metricas_test.csv"), StandardCharsets.UTF_8);
        if (lineas.size() < 2) throw new IOException("metricas_test.csv no contiene resultados.");
        String[] encabezados = lineas.get(0).split(",", -1);
        for (String linea : lineas.subList(1, lineas.size())) {
            String[] valores = linea.split(",", -1);
            if (valores.length >= encabezados.length && valores[0].startsWith("Modelo B")) {
                Map<String, String> resultado = new LinkedHashMap<>();
                for (int i = 0; i < encabezados.length; i++) resultado.put(encabezados[i].trim(), valores[i].trim());
                return resultado;
            }
        }
        throw new IOException("No se encontró la fila del Modelo B en metricas_test.csv.");
    }
}
