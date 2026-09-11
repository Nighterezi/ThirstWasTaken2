package com.thirstwastaken2.dev;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.dev.benchmark.BenchmarkCommand;
import com.thirstwastaken2.dev.benchmark.BenchmarkRunner;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

/**
 * Entry point of the dev tools mod. Nothing is registered unless {@link ThirstWasTaken2#DEV} is set, and
 * the mod itself only exists in the {@code dev} source set, which no published jar contains.
 */
public final class ThirstDev implements ModInitializer {
    /** {@code -Dthirstwastaken2.benchmark=<arguments>} runs {@code /thirst benchmark <arguments>} once the server is up. */
    public static final String AUTORUN_PROPERTY = "thirstwastaken2.benchmark";
    /** {@code -Dthirstwastaken2.benchmark.exit=true} stops the server when that run is over, however it ended. */
    public static final String EXIT_PROPERTY = "thirstwastaken2.benchmark.exit";

    @Override
    public void onInitialize() {
        if (!ThirstWasTaken2.DEV) {
            ThirstWasTaken2.LOGGER.warn("thirstwastaken2-dev is loaded outside a dev run; /thirst benchmark stays disabled");
            return;
        }
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> BenchmarkCommand.register(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(BenchmarkRunner::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(BenchmarkRunner::stop);
        ServerLifecycleEvents.SERVER_STARTED.register(ThirstDev::autorun);
    }

    /** Runs the benchmark the launch asked for, as if it had been typed into the server console. */
    private static void autorun(MinecraftServer server) {
        String arguments = System.getProperty(AUTORUN_PROPERTY);
        if (arguments == null) return;
        boolean exit = Boolean.getBoolean(EXIT_PROPERTY);
        String command = ("thirst benchmark " + arguments.trim()).trim();
        BenchmarkRunner.LOGGER.info("[ThirstBenchmark] autorun: /{}{}", command,
                exit ? " (the server stops when it is done)" : "");
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
        if (BenchmarkRunner.isRunning()) {
            BenchmarkRunner.exitWhenDone(exit);
            return;
        }
        // A typo in -Pbenchmark would otherwise leave an unattended server running forever.
        BenchmarkRunner.LOGGER.error("[ThirstBenchmark] DONE status=failed: '/{}' did not start a benchmark", command);
        if (exit) server.halt(false);
    }
}
