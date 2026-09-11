package com.thirstwastaken2.dev.benchmark;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.config.ThirstConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/**
 * One benchmark from start to report. Stages run in order over as many server ticks as they need. However
 * the run ends, completed, cancelled or failed, it restores the world and writes a report.
 */
final class BenchmarkRun {
    /** Benchmark work per server tick, leaving the rest of the 50 ms to the server itself. */
    private static final long TICK_BUDGET_NANOS = 35_000_000L;
    /** Time for the forced chunks, and the neighbours they pull in, to finish loading before the heap baseline. */
    private static final int CHUNK_SETTLE_TICKS = 40;
    private static final double MIB = 1024.0 * 1024.0;

    private final MinecraftServer server;
    private final CommandSourceStack source;
    private final BenchmarkProfile profile;
    private final BenchmarkWorld world;
    private final Deque<Stage> stages = new ArrayDeque<>();
    private final int totalStages;
    private final List<TickScenario> tickScenarios = new ArrayList<>();
    private final InteractionScenario interactions;
    private final MemoryProbe memory;
    private final long startedNanos = System.nanoTime();
    private final String startedAt = OffsetDateTime.now().toString();
    private long heapBeforeBytes = -1;
    private long gcCountBefore;
    private long gcMillisBefore;
    private String cancelReason;
    private boolean finished;

    BenchmarkRun(CommandSourceStack source, BenchmarkProfile profile) {
        this.server = source.getServer();
        this.source = source;
        this.profile = profile;
        this.world = new BenchmarkWorld(server);
        stages.add(Stage.once("benchmark area", world::open));
        stages.add(Stage.idle("chunk settle", CHUNK_SETTLE_TICKS));
        // After the area has loaded, so the chunks it holds are in both heap figures and cancel out.
        stages.add(Stage.once("heap baseline", this::baseline));
        for (int count : profile.playerCounts()) {
            TickScenario scenario = new TickScenario(world, count, profile.warmupTicks(), profile.measuredTicks());
            tickScenarios.add(scenario);
            stages.add(scenario);
        }
        interactions = new InteractionScenario(world, profile.interactionOps());
        stages.add(interactions);
        memory = new MemoryProbe(world);
        stages.add(memory);
        totalStages = stages.size();
    }

    boolean isFinished() {
        return finished;
    }

    void cancel(String reason) {
        if (cancelReason == null) cancelReason = reason;
    }

    String status() {
        Stage stage = stages.peek();
        String where = stage == null
                ? "writing the report"
                : stage.name() + (stage.progress().isEmpty() ? "" : " (" + stage.progress() + ")");
        int number = Math.min(totalStages, totalStages - stages.size() + 1);
        return String.format(Locale.ROOT, "Thirst benchmark '%s': stage %d/%d, %s, %.1f s elapsed",
                profile.name(), number, totalStages, where, seconds());
    }

    void tick() {
        if (finished) return;
        if (cancelReason != null) {
            finish("cancelled", cancelReason, null);
            return;
        }
        long deadline = System.nanoTime() + TICK_BUDGET_NANOS;
        try {
            while (!stages.isEmpty()) {
                Stage stage = stages.peek();
                if (!stage.run(deadline)) return;
                stages.poll();
                logFinished(stage);
                if (!stages.isEmpty() && System.nanoTime() >= deadline) return;
            }
        } catch (Throwable error) {
            finish("failed", error.toString(), error);
            return;
        }
        finish("ok", null, null);
    }

    private void baseline() {
        heapBeforeBytes = Metrics.heapUsedAfterGcBytes();
        gcCountBefore = Metrics.gcCount();
        gcMillisBefore = Metrics.gcMillis();
    }

    private void logFinished(Stage stage) {
        BenchmarkRunner.LOGGER.info("[ThirstBenchmark] finished {} at {} s", stage.name(),
                String.format(Locale.ROOT, "%.1f", seconds()));
        if (stage instanceof TickScenario scenario && scenario.result() != null) {
            JsonObject result = scenario.result();
            JsonObject msPerTick = result.getAsJsonObject("msPerTick");
            BenchmarkRunner.LOGGER.info("[ThirstBenchmark]   {} players: {} ms/tick mean, {} ms p99, {} us/player, {} bytes/player/tick",
                    result.get("players"), msPerTick.get("mean"), msPerTick.get("p99"),
                    result.get("microsPerPlayerPerTick"), result.get("allocatedBytesPerPlayerPerTick"));
        }
    }

    private void finish(String status, String message, Throwable error) {
        finished = true;
        if (error != null) BenchmarkRunner.LOGGER.error("[ThirstBenchmark] the run failed", error);

        // Let go of every simulated player before the heap is measured again, but keep the area loaded
        // until afterwards, so the two heap figures differ by what the run left behind and not by chunks.
        for (TickScenario scenario : tickScenarios) scenario.release();
        interactions.release();
        memory.release();
        world.releasePlayers();
        long heapAfterBytes = heapBeforeBytes >= 0 ? Metrics.heapUsedAfterGcBytes() : -1;
        try {
            world.close();
        } catch (Throwable closeError) {
            BenchmarkRunner.LOGGER.error("[ThirstBenchmark] could not restore the benchmark area", closeError);
        }

        JsonObject report = report(status, message, heapAfterBytes);
        Path file = BenchmarkReport.write(report);
        List<String> lines = BenchmarkReport.format(report, file);
        for (String line : lines) BenchmarkRunner.LOGGER.info("[ThirstBenchmark] {}", line);
        if (source.isPlayer()) {
            for (String line : lines) source.sendSuccess(() -> Component.literal(line), false);
        } else {
            source.sendSuccess(() -> Component.literal("Thirst benchmark finished with status " + status
                    + (file != null ? ", report written to " + file : "")), false);
        }
        BenchmarkRunner.LOGGER.info("[ThirstBenchmark] DONE status={} in {} s{}", status,
                String.format(Locale.ROOT, "%.1f", seconds()), file != null ? " report=" + file : "");
    }

    private JsonObject report(String status, String message, long heapAfterBytes) {
        JsonObject json = new JsonObject();
        json.addProperty("tool", "thirstwastaken2 benchmark");
        json.addProperty("schema", 1);
        json.addProperty("status", status);
        if (message != null) json.addProperty("message", message);
        json.addProperty("startedAt", startedAt);
        json.addProperty("durationSeconds", Metrics.round(seconds()));
        json.add("profile", profile.toJson());
        json.add("environment", environment());
        json.add("config", config());

        JsonArray ticks = new JsonArray();
        for (TickScenario scenario : tickScenarios) {
            if (scenario.result() != null) ticks.add(scenario.result());
        }
        json.add("tickScenarios", ticks);
        json.add("interactions", interactions.results());

        JsonObject memoryJson = memory.result() != null ? memory.result().deepCopy() : new JsonObject();
        if (heapBeforeBytes >= 0 && heapAfterBytes >= 0) {
            memoryJson.addProperty("heapUsedAfterGcBeforeMiB", Metrics.round(heapBeforeBytes / MIB));
            memoryJson.addProperty("heapUsedAfterGcAfterMiB", Metrics.round(heapAfterBytes / MIB));
            memoryJson.addProperty("heapRetainedKiB", Metrics.round((heapAfterBytes - heapBeforeBytes) / 1024.0));
            memoryJson.addProperty("gcCollectionsDuringRun", Metrics.gcCount() - gcCountBefore);
            memoryJson.addProperty("gcMillisDuringRun", Metrics.gcMillis() - gcMillisBefore);
        }
        json.add("memory", memoryJson);
        return json;
    }

    private JsonObject environment() {
        JsonObject json = new JsonObject();
        json.addProperty("minecraft", ThirstWasTaken2.MINECRAFT);
        json.addProperty("modVersion", FabricLoader.getInstance().getModContainer(ThirstWasTaken2.MOD_ID)
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown"));
        json.addProperty("dev", ThirstWasTaken2.DEV);
        json.addProperty("dedicatedServer", server.isDedicatedServer());
        json.addProperty("java", System.getProperty("java.version"));
        json.addProperty("vm", System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version"));
        json.addProperty("os", System.getProperty("os.name") + " " + System.getProperty("os.version")
                + " (" + System.getProperty("os.arch") + ")");
        json.addProperty("availableProcessors", Runtime.getRuntime().availableProcessors());
        json.addProperty("maxHeapMiB", Metrics.round(Runtime.getRuntime().maxMemory() / MIB));
        json.addProperty("allocationTracking", Metrics.allocationTracking());
        json.addProperty("difficulty", world.level.getDifficulty().name());
        json.addProperty("benchmarkArea", world.describe());
        return json;
    }

    private static JsonObject config() {
        ThirstConfig config = ThirstConfig.get();
        JsonObject json = new JsonObject();
        json.addProperty("thirstDepletionModifier", config.thirstDepletionModifier);
        json.addProperty("netherThirstDepletionModifier", config.netherThirstDepletionModifier);
        json.addProperty("depletesWhenNauseous", config.depletesWhenNauseous);
        json.addProperty("preventSprintingWhenThirsty", config.preventSprintingWhenThirsty);
        json.addProperty("dehydrationHaltsHealthRegen", config.dehydrationHaltsHealthRegen);
        json.addProperty("enableKeywordMatching", config.enableKeywordMatching);
        return json;
    }

    private double seconds() {
        return (System.nanoTime() - startedNanos) / 1e9;
    }
}
