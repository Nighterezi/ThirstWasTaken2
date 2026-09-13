package com.thirstwastaken2.client.fabric;

import com.thirstwastaken2.client.ThirstWasTaken2Client;
import net.fabricmc.api.ClientModInitializer;

/** The Fabric {@code client} entrypoint. Everything it starts is loader independent. */
public final class ThirstWasTaken2FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ThirstWasTaken2Client.initialize();
    }
}
