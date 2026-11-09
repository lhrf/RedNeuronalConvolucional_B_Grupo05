package com.grupo05.cnn_b;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/** Mantiene un único proceso Python local y su protocolo JSON Lines. */
public final class InferenceBackend implements AutoCloseable {
    private final Process process;
    private final BufferedWriter input;
    private final BufferedReader output;
    private final Gson gson = new Gson();

    public InferenceBackend(Path root) throws IOException {
        Path packaged = root.resolve("backend/CNNBBackend/CNNBBackend.exe");
        if (packaged.toFile().isFile()) {
            process = new ProcessBuilder(packaged.toString()).directory(root.toFile()).start();
            input = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
            output = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            startErrorLogger();
            return;
        }
        Path python = root.resolve("backend/.venv/Scripts/python.exe");
        if (!python.toFile().isFile()) throw new FileNotFoundException("No se encontró backend/.venv. Cree el entorno e instale backend/requirements.txt.");
        process = new ProcessBuilder(python.toString(), root.resolve("backend/servidor_modelo.py").toString())
                .directory(root.toFile()).start();
        input = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
        output = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        startErrorLogger();
    }

    private void startErrorLogger() {
        Thread logs = new Thread(() -> {
            try (BufferedReader errors = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = errors.readLine()) != null) System.err.println("[Python] " + line);
            } catch (IOException ignored) { }
        }, "cnn-b-python-logs");
        logs.setDaemon(true);
        logs.start();
    }

    public synchronized void awaitReady() throws IOException {
        JsonObject response = readResponse();
        if (!response.has("ready") || !response.get("ready").getAsBoolean())
            throw new IOException(response.has("error") ? response.get("error").getAsString() : "El modelo no quedó listo.");
    }

    public synchronized JsonObject predict(Path image) throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("cmd", "predict");
        request.addProperty("path", image.toAbsolutePath().normalize().toString());
        input.write(gson.toJson(request)); input.newLine(); input.flush();
        JsonObject response = readResponse();
        if (response.has("ok") && !response.get("ok").getAsBoolean())
            throw new IOException(response.get("error").getAsString());
        return response;
    }

    private JsonObject readResponse() throws IOException {
        String line = output.readLine();
        if (line == null) throw new IOException("El proceso Python se cerró antes de responder.");
        return gson.fromJson(line, JsonObject.class);
    }

    @Override public synchronized void close() {
        if (process.isAlive()) {
            try { input.write("{\"cmd\":\"shutdown\"}"); input.newLine(); input.flush(); } catch (IOException ignored) { }
            try { if (!process.waitFor(3, TimeUnit.SECONDS)) process.destroy(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); process.destroy(); }
        }
    }
}
