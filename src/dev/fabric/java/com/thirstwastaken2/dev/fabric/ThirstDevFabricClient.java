package com.thirstwastaken2.dev.fabric;

import com.thirstwastaken2.dev.ThirstDevClient;
import net.fabricmc.api.ClientModInitializer;

/** The dev tools mod's client entrypoint on Fabric: the agent, polled on the client tick. */
public final class ThirstDevFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ThirstDevClient.initialize();
    }
}
