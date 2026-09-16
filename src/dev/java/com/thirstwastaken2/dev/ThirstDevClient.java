package com.thirstwastaken2.dev;

import com.thirstwastaken2.dev.agent.thirst.ClientWindow;
import com.thirstwastaken2.dev.agent.thirst.ThirstAgent;
import com.thirstwastaken2.dev.platform.DevClientLoader;
import net.minecraft.client.Minecraft;

/**
 * What the dev tools mod registers on a client. The client half of {@link ThirstDev}, kept apart from
 * it because a dedicated server must never load a class that names {@link Minecraft}.
 *
 * <p>The queue is polled on the client tick rather than the server tick, even when the client has an
 * integrated server. One process has one queue, and it belongs to the side that can answer about the
 * HUD and the framebuffer; {@code server.*} commands reach the integrated server from there.
 */
public final class ThirstDevClient {
    private static boolean started;

    private ThirstDevClient() { }

    public static void initialize() {
        if (!ThirstDev.enabled()) return;
        ThirstAgent.install(true, () -> Minecraft.getInstance().stop());
        ThirstDev.registerCommand();
        // Opened on the first tick rather than at initialization: the window, the framebuffer and the
        // key mappings all exist by then, and a script may ask about any of them in its first line.
        DevClientLoader.onClientTickEnd(() -> {
            if (!started) {
                started = true;
                // An unattended client has no focused window. Leaving vanilla's focus pause on would
                // keep opening PauseScreen and make client.hold report stationary keys rather than
                // the player's movement state the probe is meant to exercise.
                Minecraft.getInstance().options.pauseOnLostFocus = false;
                // And, when this client is driven rather than played, it opens maximised and stops
                // taking the mouse pointer, so the desktop around it stays usable. See ClientWindow.
                ClientWindow.open(Minecraft.getInstance());
                ThirstAgent.start();
            }
            ThirstAgent.tick();
        });
        DevClientLoader.onClientStopping(ThirstAgent::flush);
    }
}
