package com.thirstwastaken2.gametest.platform;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.function.Consumer;

/**
 * A simulated player's connection that reports the mod's channel and keeps what was sent to it, for
 * NeoForge.
 *
 * <p>This is the piece the plan's last phase turns on. The mod syncs a player's thirst to that player
 * and no one else, and NeoForge decides it twice: the mod's own predicate, and then
 * {@code connection.hasChannel(SyncAttachmentsPayload.TYPE)}. A benchmark-style simulated player fails
 * the second test — its connection negotiated nothing — so nothing is ever sent to it and a test built
 * on one would pass whatever the predicate said. Reporting the channel is therefore the whole trick,
 * and on NeoForge it is one method: the listener answers {@code hasChannel} itself, on every supported
 * version.
 *
 * <p>Nothing is written to a socket. {@code send} collects the packet instead, which is what the test
 * reads. See {@code docs/dev/AGENT-CLIENT-PLAN.md} and {@code src/gametest/java/AGENTS.md}.
 */
public final class CapturingConnection {
    private CapturingConnection() { }

    /** Whether this loader can report a negotiated channel from a test connection. */
    public static boolean available() {
        return true;
    }

    /** Why it cannot, for the test to log when {@link #available} is false. */
    public static String unavailable() {
        return "";
    }

    /** Replaces {@code player}'s connection with one that reports the channel and keeps what is sent. */
    public static void install(ServerPlayer player, Consumer<Packet<?>> sink) {
        new CapturingListener(player, sink);
    }

    /**
     * Assigns itself to {@code player.connection}, the way the real listener does, so constructing one
     * is all a caller has to do.
     */
    private static final class CapturingListener extends ServerGamePacketListenerImpl {
        private final Consumer<Packet<?>> sink;

        private CapturingListener(ServerPlayer player, Consumer<Packet<?>> sink) {
            super(player.level().getServer(), new SilentConnection(), player,
                    CommonListenerCookie.createInitial(player.getGameProfile(), false));
            this.sink = sink;
        }

        /** The answer that makes the sync reach a simulated player at all. */
        @Override
        public boolean hasChannel(CustomPacketPayload.Type<?> type) {
            return true;
        }

        @Override
        public void send(Packet<?> packet) {
            sink.accept(packet);
        }

        // The listener the second argument takes became Netty's own in 1.21.11.
        //? if >1.21.1 {
        @Override
        public void send(Packet<?> packet, io.netty.channel.ChannelFutureListener listener) {
            sink.accept(packet);
        }
        //?} else {
        /*@Override
        public void send(Packet<?> packet, net.minecraft.network.PacketSendListener listener) {
            sink.accept(packet);
        }
        *///?}
    }

    /** A connection with no channel behind it. Nothing is ever written to it; {@code send} is overridden. */
    private static final class SilentConnection extends Connection {
        private SilentConnection() {
            super(PacketFlow.CLIENTBOUND);
        }
    }
}
