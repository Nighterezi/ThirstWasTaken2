package com.thirstwastaken2.gametest.platform;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

/**
 * A simulated player's connection that reports the mod's channel — which this loader has no way to
 * produce, so on Fabric there is none.
 *
 * <p>The NeoForge copy of this class works because its listener answers
 * {@code hasChannel(SyncAttachmentsPayload.TYPE)} itself, one overridable method on every supported
 * version. Fabric decides the same thing from a set of attachment ids the real client sends during
 * configuration and that its networking layer keeps on the connection's own packet context. A
 * simulated player never goes through configuration, the set stays empty, and the sync is dropped
 * before it reaches any connection this test could capture. Reaching in to write that set means
 * reaching into an implementation package whose shape has moved between versions, which buys a second
 * copy of a check the NeoForge node already makes.
 *
 * <p>So the phase this belongs to is NeoForge only, and deliberately: the thing it exists to catch is
 * {@code Loader.syncsTo}, which is NeoForge's function. Fabric's own answer is
 * {@code AttachmentSyncPredicate.targetOnly()}, a value handed to Fabric API rather than a decision
 * the mod makes, and the check for it is still a client with eyes on it — or two agent clients,
 * which is what {@code runManualA} and {@code runManualB} are for. See {@code src/dev/java/com/thirstwastaken2/dev/agent/AGENTS.md}.
 */
public final class CapturingConnection {
    private CapturingConnection() { }

    /** Whether this loader can report a negotiated channel from a test connection. */
    public static boolean available() {
        return false;
    }

    /** Why it cannot, for the test to log when {@link #available} is false. */
    public static String unavailable() {
        return "Fabric keeps the negotiated attachment channels on the connection's packet context, "
                + "which only a real client's configuration phase fills in; per-player sync is checked "
                + "on the NeoForge nodes and with two agent clients instead";
    }

    /** Never called on this loader: {@link #available} is false. */
    public static void install(ServerPlayer player, Consumer<Packet<?>> sink) {
        throw new UnsupportedOperationException(unavailable());
    }
}
