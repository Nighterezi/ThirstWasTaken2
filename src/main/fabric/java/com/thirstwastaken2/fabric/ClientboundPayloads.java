package com.thirstwastaken2.fabric;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The client halves of {@code Loader.clientboundPayload}, held until the client entrypoint registers
 * them. Fabric API keeps {@code ClientPlayNetworking} in its client module, which the main source set
 * cannot see, and the main entrypoint always runs before the client one.
 */
public final class ClientboundPayloads {
    private static final List<Registration<?>> REGISTRATIONS = new ArrayList<>();

    public record Registration<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, Consumer<T> handler) { }

    private ClientboundPayloads() { }

    public static <T extends CustomPacketPayload> void add(CustomPacketPayload.Type<T> type, Consumer<T> handler) {
        REGISTRATIONS.add(new Registration<>(type, handler));
    }

    public static List<Registration<?>> all() {
        return List.copyOf(REGISTRATIONS);
    }
}
