package com.thirstwastaken2.dev;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.dev.agent.thirst.AgentCommand;
import com.thirstwastaken2.dev.agent.thirst.ThirstAgent;
import com.thirstwastaken2.dev.benchmark.BenchmarkCommand;
import com.thirstwastaken2.dev.benchmark.BenchmarkRunner;
import com.thirstwastaken2.dev.harness.Autorun;
import com.thirstwastaken2.dev.platform.DevLoader;
import com.thirstwastaken2.platform.Loader;
import net.minecraft.server.MinecraftServer;

/**
 * What the dev tools mod registers on a server, whichever loader it is: the agent's queue, its
 * {@code /thirst agent} command and the tick that polls it, and {@code /thirst benchmark} with the
 * tick that advances a run.
 *
 * <p>Nothing is registered unless {@link ThirstWasTaken2#DEV} is true. That flag is the loader's own
 * development-environment check, true under every run task and false in the jar players install, and
 * this source set is not in that jar either way.
 *
 * <p>This class is safe to load on a dedicated server: it names no client class. The client half is
 * {@link ThirstDevClient}, which the client entrypoint calls instead.
 */
public final class ThirstDev {
    /**
     * {@code -Dthirstwastaken2.benchmark=<arguments>} runs {@code /thirst benchmark <arguments>} once the
     * server is up, and {@code -Dthirstwastaken2.benchmark.exit=true} stops the server when that run is
     * over, however it ended. {@link Autorun} reads the pair, the same way it reads the agent's.
     */
    public static final String BENCHMARK_AUTORUN_PROPERTY = "thirstwastaken2.benchmark";

    private ThirstDev() { }

    /**
     * Installs the agent on a dedicated server. Its queue is polled on the server tick and opened once
     * the server is up, which is also when a launch's script runs.
     */
    public static void initializeServer() {
        if (!enabled()) return;
        ThirstAgent.install(false, ThirstDev::halt);
        registerCommand();
        Loader.onServerTickEnd(ThirstAgent::serverTick);
        DevLoader.onServerStarted(server -> ThirstAgent.start());
        DevLoader.onServerStopping(server -> ThirstAgent.flush());
    }

    /**
     * Installs {@code /thirst benchmark} on whichever server this process has: a dedicated one, or a
     * client's integrated one. The run is advanced on the server tick, ended if the server stops under it,
     * and started on its own when the launch asked for one.
     *
     * <p>Separate from {@link #initializeServer()} because the two tools are wanted in different places: a
     * client installs the agent through {@link ThirstDevClient} and the benchmark through here, and a
     * dedicated server takes both.
     */
    public static void installBenchmark() {
        if (!enabled()) return;
        Loader.onRegisterCommands(BenchmarkCommand::register);
        Loader.onServerTickEnd(BenchmarkRunner::tick);
        DevLoader.onServerStopping(BenchmarkRunner::stop);
        DevLoader.onServerStarted(ThirstDev::autorunBenchmark);
    }

    /** Runs the benchmark the launch asked for, as if it had been typed into the server console. */
    private static void autorunBenchmark(MinecraftServer server) {
        Autorun requested = Autorun.of(BENCHMARK_AUTORUN_PROPERTY);
        if (requested == null) return;
        boolean exit = requested.stopWhenDone();
        String command = ("thirst benchmark " + requested.argument()).trim();
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

    /** Whether the dev tools should register anything at all, with the reason logged when they should not. */
    public static boolean enabled() {
        if (ThirstWasTaken2.DEV) return true;
        ThirstWasTaken2.LOGGER.warn("thirstwastaken2-dev is loaded outside a development run; "
                + "the agent and /thirst benchmark stay disabled");
        return false;
    }

    /** Adds {@code /thirst agent} to the server's command tree, on a client's integrated server too. */
    public static void registerCommand() {
        Loader.onRegisterCommands(AgentCommand::register);
    }

    private static void halt() {
        MinecraftServer server = ThirstAgent.server();
        if (server != null) server.halt(false);
    }
}
