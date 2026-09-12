package com.thirstwastaken2.dev.benchmark;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Writes a finished run's report to disk and renders the same data as console lines. */
final class BenchmarkReport {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.ROOT);
    private static final String TICK_ROW = "%8s %10s %10s %9s %11s %12s %13s %10s";
    private static final String OPERATION_ROW = "%-22s %10s %10s %11s %12s";

    private BenchmarkReport() { }

    /**
     * Writes the report to {@code <run directory>/benchmark/thirst-benchmark-<timestamp>.json} and copies
     * it to {@code latest.json} next to it. Returns the timestamped file, or null when writing failed.
     */
    static Path write(JsonObject report) {
        try {
            Path directory = FabricLoader.getInstance().getGameDir().resolve("benchmark");
            Files.createDirectories(directory);
            String json = GSON.toJson(report);
            Path file = directory.resolve("thirst-benchmark-" + LocalDateTime.now().format(STAMP) + ".json");
            Files.writeString(file, json, StandardCharsets.UTF_8);
            Files.writeString(directory.resolve("latest.json"), json, StandardCharsets.UTF_8);
            return file.toAbsolutePath();
        } catch (IOException | RuntimeException e) {
            BenchmarkRunner.LOGGER.error("[ThirstBenchmark] could not write the report", e);
            return null;
        }
    }

    /** The report as console lines: environment, one table per scenario kind, then memory. */
    static List<String> format(JsonObject report, Path file) {
        List<String> lines = new ArrayList<>();
        JsonObject environment = object(report, "environment");
        lines.add("Minecraft " + text(environment, "minecraft") + ", mod " + text(environment, "modVersion")
                + ", Java " + text(environment, "java") + ", " + text(environment, "availableProcessors") + " CPUs, "
                + text(environment, "maxHeapMiB") + " MiB max heap, profile " + text(object(report, "profile"), "name"));
        lines.add("Area: " + text(environment, "benchmarkArea"));

        lines.add("Per server tick, mod work only (a tick has 50 ms):");
        lines.add(String.format(Locale.ROOT, TICK_ROW,
                "players", "mean ms", "p99 ms", "budget %", "us/player", "bytes/tick", "bytes/player", "sync/p/s"));
        for (JsonElement element : array(report, "tickScenarios")) {
            JsonObject scenario = element.getAsJsonObject();
            JsonObject msPerTick = object(scenario, "msPerTick");
            lines.add(String.format(Locale.ROOT, TICK_ROW,
                    text(scenario, "players"), text(msPerTick, "mean"), text(msPerTick, "p99"),
                    text(scenario, "tickBudgetPercent"), text(scenario, "microsPerPlayerPerTick"),
                    text(scenario, "allocatedBytesPerTick"), text(scenario, "allocatedBytesPerPlayerPerTick"),
                    text(scenario, "syncPacketsPerPlayerPerSecond")));
            StringBuilder shares = new StringBuilder("         time share:");
            for (Map.Entry<String, JsonElement> section : object(scenario, "sections").entrySet()) {
                shares.append(' ').append(section.getKey()).append(' ')
                        .append(text(section.getValue().getAsJsonObject(), "sharePercent")).append('%');
            }
            lines.add(shares.toString());
        }

        lines.add("Interactions, one player, per operation:");
        lines.add(String.format(Locale.ROOT, OPERATION_ROW, "operation", "mean us", "p99 us", "bytes/op", "ops/ms"));
        for (JsonElement element : array(report, "interactions")) {
            JsonObject operation = element.getAsJsonObject();
            JsonObject micros = object(operation, "microsPerOp");
            lines.add(String.format(Locale.ROOT, OPERATION_ROW,
                    text(operation, "name"), text(micros, "mean"), text(micros, "p99"),
                    text(operation, "bytesPerOp"), text(operation, "opsPerMillisecond")));
        }

        JsonObject memory = object(report, "memory");
        lines.add("Memory: ThirstData " + text(memory, "thirstDataBytes") + " B, ExhaustionTracker "
                + text(memory, "exhaustionTrackerBytes") + " B, first touch " + text(memory, "firstTouchBytesPerPlayer")
                + " B/player, steady tick " + text(memory, "steadyTickBytesPerPlayer") + " B/player, thirst cache "
                + text(memory, "thirstCacheEntries") + " entries, purity cache " + text(memory, "purityInfoEntries")
                + " entries");
        lines.add("Heap after GC: " + text(memory, "heapUsedAfterGcBeforeMiB") + " MiB before, "
                + text(memory, "heapUsedAfterGcAfterMiB") + " MiB after ("
                + text(memory, "heapRetainedKiB") + " KiB retained); "
                + text(memory, "gcCollectionsDuringRun") + " GCs, " + text(memory, "gcMillisDuringRun") + " ms");
        if (report.has("message")) lines.add("Message: " + text(report, "message"));
        lines.add("Status " + text(report, "status") + " after " + text(report, "durationSeconds") + " s"
                + (file != null ? ", report " + file : ", report not written"));
        return lines;
    }

    private static JsonObject object(JsonObject parent, String key) {
        return parent.has(key) && parent.get(key).isJsonObject() ? parent.getAsJsonObject(key) : new JsonObject();
    }

    private static JsonArray array(JsonObject parent, String key) {
        return parent.has(key) && parent.get(key).isJsonArray() ? parent.getAsJsonArray(key) : new JsonArray();
    }

    private static String text(JsonObject parent, String key) {
        if (!parent.has(key) || parent.get(key).isJsonNull()) return "-";
        JsonElement value = parent.get(key);
        return value.isJsonPrimitive() ? value.getAsString() : value.toString();
    }
}
