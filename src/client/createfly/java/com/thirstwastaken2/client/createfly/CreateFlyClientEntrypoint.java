package com.thirstwastaken2.client.createfly;

import com.thirstwastaken2.createfly.CreateFlyPresence;
import net.fabricmc.api.ClientModInitializer;

/**
 * The {@code thirstwastaken2:createfly_client} entrypoint. Like the common one, it touches nothing that
 * names a Create class until the presence check has passed.
 */
public final class CreateFlyClientEntrypoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (CreateFlyPresence.isPresent()) SandFilterClient.register();
    }
}
