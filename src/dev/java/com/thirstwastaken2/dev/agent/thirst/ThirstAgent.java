package com.thirstwastaken2.dev.agent.thirst;

import com.google.gson.JsonObject;
import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.dev.agent.core.AgentDispatcher;
import com.thirstwastaken2.dev.agent.core.AgentQueue;
import com.thirstwastaken2.dev.harness.Autorun;
import com.thirstwastaken2.dev.harness.DevEnvironment;
import com.thirstwastaken2.dev.harness.ServerAwake;
import com.thirstwastaken2.dev.platform.DevLoader;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

/**
 * The agent, wired up: one queue per game process, polled on that process's own tick.
 *
 * <p>A dedicated server and a client of the same node share {@code run/&lt;node&gt;}, so the queue
 * directory carries a name — {@code server} or {@code client} by default,
 * {@code -Dthirstwastaken2.agent=&lt;name&gt;} otherwise — and the two never write to one file. The
 * result is {@code run/&lt;node&gt;/agent/&lt;name&gt;/}, holding {@code ready.json}, {@code in.jsonl}
 * and {@code out.jsonl}.
 *
 * <p>One queue per process rather than one per side: a client with an integrated server is still one
 * process, and splitting it in two would make an agent guess which of two directories a command
 * belongs in. {@code server.*} commands work wherever a server is running, integrated or dedicated,
 * and say so plainly when there is none.
 */
public final class ThirstAgent {
    /** {@code -Dthirstwastaken2.agent=<name>} names this process's queue directory. */
    public static final String NAME_PROPERTY = "thirstwastaken2.agent";
    /**
     * {@code -Dthirstwastaken2.agent.script=<file>} runs a file of requests once, at startup, and
     * {@code -Dthirstwastaken2.agent.script.exit=true} stops the game once it has been answered. The
     * pair is read by {@link Autorun}, which the benchmark's own autorun property goes through too.
     */
    public static final String SCRIPT_PROPERTY = "thirstwastaken2.agent.script";

    /** The queue this process owns, or null before {@link #install} or outside a development run. */
    private static AgentDispatcher dispatcher;
    private static String side;
    /** Stops this process: halting a dedicated server, or closing a client's window. */
    private static Runnable exit;
    /** The running server, integrated or dedicated; null on a client that has not loaded a world. */
    private static MinecraftServer server;

    private ThirstAgent() { }

    /**
     * Builds this process's queue and registers what it can answer. Called once from the dev tools'
     * entrypoint: {@code client} is true in a game with a window, which is the only process that can
     * answer a {@code client.*} command, and {@code exit} is how that process stops itself when a
     * script asks it to.
     */
    public static void install(boolean client, Runnable exit) {
        if (dispatcher != null) return;
        side = client ? "client" : "server";
        ThirstAgent.exit = exit;
        Path directory = DevLoader.gameDir().resolve("agent").resolve(queueName());

        JsonObject about = DevEnvironment.describe();
        about.addProperty("side", side);
        about.addProperty("name", queueName());

        dispatcher = new AgentDispatcher(new AgentQueue(directory, ThirstWasTaken2.LOGGER),
                ThirstWasTaken2.LOGGER, about);
        CommonProbes.register(dispatcher, side);
        ServerProbes.register(dispatcher);
        if (client) ClientProbes.register(dispatcher);

        DevLoader.onServerStarted(started -> server = started);
        DevLoader.onServerStopping(stopping -> server = null);
    }

    /** The dispatcher this process owns, or null when the agent is not installed. */
    public static AgentDispatcher dispatcher() {
        return dispatcher;
    }

    /** {@code server} or {@code client}: which side of the game this process's queue is polled on. */
    public static String side() {
        return side;
    }

    /** The running server, integrated or dedicated, or null when there is none. */
    public static MinecraftServer server() {
        return server;
    }

    /**
     * Opens the queue and runs the launch's script, if it named one. Called once the process can
     * answer: after the server has started on a dedicated server, and on the first client tick.
     */
    public static void start() {
        if (dispatcher == null) return;
        dispatcher.start();
        Autorun script = Autorun.of(SCRIPT_PROPERTY);
        if (script == null || script.argument().isEmpty()) return;
        dispatcher.runScript(Path.of(script.argument()), () -> {
            ThirstWasTaken2.LOGGER.info("[ThirstAgent] DONE script answered, out={}",
                    dispatcher.queue().file(AgentQueue.OUT).toAbsolutePath());
            if (script.stopWhenDone()) stop();
        });
    }

    /** Answers whatever is still deferred and closes the queue, without stopping the game. */
    public static void flush() {
        if (dispatcher != null) dispatcher.stop();
    }

    /** Closes the queue and then stops this process: halting a dedicated server, or closing a window. */
    public static void stop() {
        flush();
        if (exit != null) exit.run();
    }

    /** Called once per tick on this process's own side. */
    public static void tick() {
        if (dispatcher != null) dispatcher.tick();
    }

    /**
     * The same, on a dedicated server's tick, where the queue also has to keep the server from pausing:
     * an empty one stops ticking after {@code pause-when-empty-seconds} and stops answering with it.
     */
    public static void serverTick(MinecraftServer running) {
        if (dispatcher == null) return;
        ServerAwake.keep(running);
        dispatcher.tick();
    }

    private static String queueName() {
        String name = System.getProperty(NAME_PROPERTY);
        return name == null || name.isBlank() ? side : name.trim();
    }
}
