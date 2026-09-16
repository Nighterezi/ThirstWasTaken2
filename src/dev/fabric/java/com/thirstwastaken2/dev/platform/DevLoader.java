package com.thirstwastaken2.dev.platform;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Every call into the mod loader the dev tools make and {@code com.thirstwastaken2.platform.Loader}
 * does not, for Fabric.
 *
 * <p>The same arrangement as the mod's own seam: one copy per loader, same name, same signatures, and
 * the build compiles exactly one of them. It is deliberately small — the agent needs where the game
 * directory is, what the mod's version is and when a server starts and stops; everything else it
 * already gets from the mod's {@code Loader}.
 */
public final class DevLoader {
    /** Which loader this build compiled, for the agent's {@code probe} answer. */
    public static final String LOADER = "fabric";

    private DevLoader() { }

    /** The run directory: {@code run/<node>} for a server or a client, one per node. */
    public static Path gameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    /** The version of the mod this is loaded beside, as the loader reports it. */
    public static String modVersion(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    /** Whether another mod is loaded, which the benchmark asks about Create Fly. */
    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    /** Runs once the server is accepting commands: a dedicated one, or a client's integrated one. */
    public static void onServerStarted(Consumer<MinecraftServer> handler) {
        ServerLifecycleEvents.SERVER_STARTED.register(handler::accept);
    }

    /** Runs as the server shuts down, while the world is still there. */
    public static void onServerStopping(Consumer<MinecraftServer> handler) {
        ServerLifecycleEvents.SERVER_STOPPING.register(handler::accept);
    }
}
