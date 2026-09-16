package com.thirstwastaken2.dev.harness;

import com.google.gson.JsonObject;
import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.dev.platform.DevLoader;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * What this process is, as every dev tool reports it: which loader, which Minecraft, which build of
 * the mod, whether the DEV gate is open and where the run directory is.
 *
 * <p>Three places answer that question — the benchmark report's {@code environment} block, the agent's
 * {@code ready.json}, and the agent's {@code probe} — and all three used to compute it themselves,
 * which is how the benchmark came to read the mod's version out of {@code FabricLoader} while the
 * agent read the same version out of {@code DevLoader}. One of those compiles on one loader.
 *
 * <p>{@link #describe} is the whole block for a tool that wants it in one go; the accessors are for a
 * report that has its own field order to keep, which is what the benchmark's has.
 */
public final class DevEnvironment {
    private DevEnvironment() { }

    /** The Minecraft version this node builds against. */
    public static String minecraft() {
        return ThirstWasTaken2.MINECRAFT;
    }

    /** The mod loader this build compiled for: {@code fabric} or {@code neoforge}. */
    public static String loader() {
        return DevLoader.LOADER;
    }

    /** The version of the mod these tools are loaded beside, as the loader reports it. */
    public static String modVersion() {
        return DevLoader.modVersion(ThirstWasTaken2.MOD_ID);
    }

    /**
     * Whether this JVM was launched with something watching it: a flight recording, a profiler agent,
     * a debugger, or the interpreter.
     *
     * <p>All four make a benchmark slower and skew what it reports, so a run under any of them is a
     * recording to read rather than a measurement to keep: `-Pprofile` puts this in the report, and
     * `tools/benchmark/aggregate.py` refuses a set with one in it. Read from the JVM's own launch
     * arguments, so a profiler somebody attached by hand counts the same as one the build added.
     */
    public static boolean profiling() {
        for (String argument : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (argument.contains("StartFlightRecording")
                    || argument.startsWith("-agentpath:")
                    || argument.startsWith("-agentlib:jdwp")
                    || argument.equals("-Xdebug")
                    || argument.equals("-Xint")) {
                return true;
            }
        }
        return false;
    }

    /** Whether the DEV gate is open. False here means a tool is running where it should not be. */
    public static boolean dev() {
        return ThirstWasTaken2.DEV;
    }

    /**
     * The processor this is running on, as the operating system names it, or {@code unknown}.
     *
     * <p>The JVM does not expose it, and a benchmark figure only means anything beside another from the
     * same machine, so a report that records the number of cores and not which cores cannot be compared
     * with confidence later. Read from what is already there — the environment on Windows,
     * {@code /proc/cpuinfo} on Linux — rather than by running a program, which a tool that reports on a
     * server has no business doing.
     */
    public static String cpu() {
        String windows = System.getenv("PROCESSOR_IDENTIFIER");
        if (windows != null && !windows.isBlank()) return windows.trim();
        Path cpuinfo = Path.of("/proc/cpuinfo");
        if (Files.isReadable(cpuinfo)) {
            try (Stream<String> lines = Files.lines(cpuinfo)) {
                return lines.filter(line -> line.startsWith("model name"))
                        .map(line -> line.substring(line.indexOf(':') + 1).trim())
                        .findFirst()
                        .orElse("unknown");
            } catch (IOException | RuntimeException ignored) {
                // An unreadable /proc is not worth failing or logging over; the field says unknown.
            }
        }
        return "unknown";
    }

    /** The run directory, {@code run/<node>}: where reports, queues and screenshots are written. */
    public static String runDirectory() {
        return DevLoader.gameDir().toAbsolutePath().toString();
    }

    /** All of the above, in one object, for a tool with no field order of its own to keep. */
    public static JsonObject describe() {
        JsonObject json = new JsonObject();
        json.addProperty("loader", loader());
        json.addProperty("minecraft", minecraft());
        json.addProperty("modVersion", modVersion());
        json.addProperty("dev", dev());
        json.addProperty("runDirectory", runDirectory());
        return json;
    }
}
