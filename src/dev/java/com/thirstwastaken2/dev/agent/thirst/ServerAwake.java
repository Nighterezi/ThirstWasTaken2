package com.thirstwastaken2.dev.agent.thirst;

import com.thirstwastaken2.ThirstWasTaken2;
import net.minecraft.server.MinecraftServer;

import java.lang.reflect.Field;

/**
 * Keeps a dedicated server with nobody online from pausing, for as long as the agent's queue is open.
 *
 * <p>A dedicated server stops ticking once it has been empty for {@code pause-when-empty-seconds} (60
 * by default), and a paused server fires no tick event, so the queue stops being polled. An agent that
 * starts a server and then takes a minute to bring a client up would find the queue dead, and a script
 * that waits for something would never be answered. There is no public way to hold the pause off; the
 * field name is the Mojang name every supported version runs with in a development environment, and a
 * version that renames it logs once and loses nothing but the keep-awake.
 *
 * <p>Only a dedicated server needs this. A client's integrated server pauses on its own rules, which
 * are the player's, and the agent has no business overriding them.
 *
 * <p>{@code /thirst benchmark} does the same thing for the same reason, in {@code BenchmarkRunner},
 * and P5 of {@code docs/dev/AGENT-CLIENT-PLAN.md} is where the two become one. Until there were two of
 * them there was nothing to extract.
 */
final class ServerAwake {
    private static final Field EMPTY_TICKS = emptyTicksField();
    private static boolean warned;

    private ServerAwake() { }

    /** Resets the empty-server counter. Called every server tick while the queue is open. */
    static void keep(MinecraftServer server) {
        if (EMPTY_TICKS == null || server == null || !server.isDedicatedServer()) return;
        try {
            EMPTY_TICKS.setInt(server, 0);
        } catch (IllegalAccessException | RuntimeException e) {
            if (!warned) {
                warned = true;
                ThirstWasTaken2.LOGGER.warn("[ThirstAgent] could not keep the empty server from pausing; "
                        + "the queue stops being polled once it has been empty for "
                        + "pause-when-empty-seconds", e);
            }
        }
    }

    private static Field emptyTicksField() {
        try {
            Field field = MinecraftServer.class.getDeclaredField("emptyTicks");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException | RuntimeException e) {
            ThirstWasTaken2.LOGGER.warn("[ThirstAgent] MinecraftServer.emptyTicks not found; an empty "
                    + "server may pause and stop answering the queue", e);
            return null;
        }
    }
}
