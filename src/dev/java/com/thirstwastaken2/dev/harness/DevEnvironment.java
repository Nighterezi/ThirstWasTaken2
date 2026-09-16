package com.thirstwastaken2.dev.harness;

import com.google.gson.JsonObject;
import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.dev.platform.DevLoader;

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

    /** Whether the DEV gate is open. False here means a tool is running where it should not be. */
    public static boolean dev() {
        return ThirstWasTaken2.DEV;
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
