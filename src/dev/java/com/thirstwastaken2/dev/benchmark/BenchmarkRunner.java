package com.thirstwastaken2.dev.benchmark;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/** Owns the single benchmark run a server can have at a time. Server thread only. */
public final class BenchmarkRunner {
    public static final Logger LOGGER = LoggerFactory.getLogger("thirstwastaken2-benchmark");

    /**
     * Vanilla's count of ticks the server has spent with nobody online. A dedicated server stops ticking once
     * it reaches {@code pause-when-empty-seconds} (60 by default), and a paused server never fires
     * {@code END_SERVER_TICK}, so a run started from the console a minute after startup would never advance,
     * and a long run would stall a minute in. There is no public way to hold the pause off; the field name is
     * the Mojang name the dev environment runs with on every supported version.
     */
    private static final Field EMPTY_TICKS = emptyTicksField();

    private static BenchmarkRun current;
    private static boolean exitWhenDone;

    private BenchmarkRunner() { }

    static boolean start(CommandSourceStack source, BenchmarkProfile profile) {
        if (current != null) return false;
        // Commands are still handled while the server is paused, so this also wakes one that already paused.
        keepAwake(source.getServer());
        current = new BenchmarkRun(source, profile);
        LOGGER.info("[ThirstBenchmark] started profile '{}': {}", profile.name(), profile.describe());
        return true;
    }

    public static boolean isRunning() {
        return current != null;
    }

    static String status() {
        BenchmarkRun run = current;
        return run == null ? "idle" : run.status();
    }

    static boolean cancel() {
        BenchmarkRun run = current;
        if (run == null) return false;
        run.cancel("cancelled by command");
        return true;
    }

    /** Stops the server once the active run has finished, whatever its outcome. Used by runBenchmark. */
    public static void exitWhenDone(boolean exit) {
        exitWhenDone = exit;
    }

    /** Advances the active run. Registered on {@code END_SERVER_TICK}. */
    public static void tick(MinecraftServer server) {
        BenchmarkRun run = current;
        if (run == null) return;
        keepAwake(server);
        run.tick();
        if (!run.isFinished()) return;
        current = null;
        if (exitWhenDone) {
            exitWhenDone = false;
            LOGGER.info("[ThirstBenchmark] stopping the server as requested");
            server.halt(false);
        }
    }

    /** Ends an active run while the server shuts down, so the benchmark area is still restored. */
    public static void stop(MinecraftServer server) {
        BenchmarkRun run = current;
        if (run == null) return;
        run.cancel("server stopping");
        run.tick();
        current = null;
    }

    /** Resets the empty-server counter so the server keeps ticking while a run needs it to. */
    private static void keepAwake(MinecraftServer server) {
        if (EMPTY_TICKS == null) return;
        try {
            EMPTY_TICKS.setInt(server, 0);
        } catch (IllegalAccessException | RuntimeException e) {
            LOGGER.warn("[ThirstBenchmark] could not keep the empty server from pausing", e);
        }
    }

    private static Field emptyTicksField() {
        try {
            Field field = MinecraftServer.class.getDeclaredField("emptyTicks");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.warn("[ThirstBenchmark] MinecraftServer.emptyTicks not found; an empty server may pause a run", e);
            return null;
        }
    }
}
