package com.thirstwastaken2.dev;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.dev.agent.thirst.AgentCommand;
import com.thirstwastaken2.dev.agent.thirst.ThirstAgent;
import com.thirstwastaken2.dev.platform.DevLoader;
import com.thirstwastaken2.platform.Loader;
import net.minecraft.server.MinecraftServer;

/**
 * What the dev tools mod registers on a server, whichever loader it is: the agent's queue, its
 * {@code /thirst agent} command and the tick that polls it.
 *
 * <p>Nothing is registered unless {@link ThirstWasTaken2#DEV} is true. That flag is the loader's own
 * development-environment check, true under every run task and false in the jar players install, and
 * this source set is not in that jar either way.
 *
 * <p>This class is safe to load on a dedicated server: it names no client class. The client half is
 * {@link ThirstDevClient}, which the client entrypoint calls instead.
 */
public final class ThirstDev {
    private ThirstDev() { }

    /**
     * Installs the agent on a dedicated server. Its queue is polled on the server tick and opened once
     * the server is up, which is also when a launch's script runs.
     */
    public static void initializeServer() {
        if (!enabled()) return;
        ThirstAgent.install(false, ThirstDev::halt);
        registerCommand();
        Loader.onServerTickEnd(server -> ThirstAgent.tick());
        DevLoader.onServerStarted(server -> ThirstAgent.start());
        DevLoader.onServerStopping(server -> ThirstAgent.flush());
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
