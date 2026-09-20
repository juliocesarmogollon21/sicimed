package com.sicimed.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class JsonUtil {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, t, c) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDate.class,
                    (JsonDeserializer<LocalDate>) (json, t, c) -> LocalDate.parse(json.getAsString()))
            .registerTypeAdapter(LocalTime.class,
                    (JsonSerializer<LocalTime>) (src, t, c) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalTime.class,
                    (JsonDeserializer<LocalTime>) (json, t, c) -> LocalTime.parse(json.getAsString()))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, t, c) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, t, c) -> LocalDateTime.parse(json.getAsString()))
            .create();

    private JsonUtil() {}

    public static Gson gson() { return GSON; }

    public static <T> T fromJson(HttpServletRequest request, Class<T> type) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return GSON.fromJson(sb.toString(), type);
    }

    public static void writeJson(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.print(GSON.toJson(body));
        }
    }

    public static void writeError(HttpServletResponse response, int status, String message) throws IOException {
        writeJson(response, status, java.util.Map.of("status", status, "message", message));
    }
}
