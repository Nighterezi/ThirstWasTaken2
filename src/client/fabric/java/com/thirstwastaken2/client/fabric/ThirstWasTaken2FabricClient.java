package com.thirstwastaken2.client.fabric;

import com.thirstwastaken2.client.ThirstWasTaken2Client;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/** The Fabric {@code client} entrypoint. Everything it starts is loader independent. */
public final class ThirstWasTaken2FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ThirstWasTaken2Client.initialize();
        // The Sand Filter's goggle tooltip, declared only by the builds that compile it.
        FabricLoader.getInstance().getEntrypoints("thirstwastaken2:createfly_client", ClientModInitializer.class)
                .forEach(ClientModInitializer::onInitializeClient);
    }
}
