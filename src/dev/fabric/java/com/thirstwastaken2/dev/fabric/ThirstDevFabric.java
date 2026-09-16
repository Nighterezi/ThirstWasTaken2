package com.thirstwastaken2.dev.fabric;

import com.thirstwastaken2.dev.ThirstDev;
import com.thirstwastaken2.dev.benchmark.BenchmarkCommand;
import com.thirstwastaken2.dev.benchmark.BenchmarkRunner;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

/**
 * The dev tools mod's main entrypoint on Fabric: {@code /thirst benchmark}, and the agent on a
 * dedicated server.
 *
 * <p>The benchmark stays here rather than in the shared source directory because it simulates players
 * with Fabric's {@code FakePlayer}, which has no counterpart on NeoForge and no place in the agent.
 * The two share a source set, not a purpose; see {@code docs/dev/AGENT-CLIENT-PLAN.md}.
 *
 * <p>Fabric runs this entrypoint on a client as well, where the agent belongs to
 * {@link ThirstDevFabricClient} instead, so the server-side install is skipped there.
 */
public final class ThirstDevFabric implements ModInitializer {
    /** {@code -Dthirstwastaken2.benchmark=<arguments>} runs {@code /thirst benchmark <arguments>} once the server is up. */
    public static final String AUTORUN_PROPERTY = "thirstwastaken2.benchmark";
    /** {@code -Dthirstwastaken2.benchmark.exit=true} stops the server when that run is over, however it ended. */
    public static final String EXIT_PROPERTY = "thirstwastaken2.benchmark.exit";

    @Override
    public void onInitialize() {
        if (!ThirstDev.enabled()) return;
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> BenchmarkCommand.register(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(BenchmarkRunner::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(BenchmarkRunner::stop);
        ServerLifecycleEvents.SERVER_STARTED.register(ThirstDevFabric::autorun);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) ThirstDev.initializeServer();
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
