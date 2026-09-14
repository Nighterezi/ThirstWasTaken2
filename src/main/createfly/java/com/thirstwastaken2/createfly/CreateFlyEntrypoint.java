package com.thirstwastaken2.createfly;

import net.fabricmc.api.ModInitializer;

/**
 * The {@code thirstwastaken2:createfly} entrypoint, run by {@code ThirstWasTaken2Fabric} once the mod
 * itself is initialized. It references {@link SandFilter} only after the presence check, so the
 * classes that extend Create's are never loaded without Create Fly.
 */
public final class CreateFlyEntrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        if (CreateFlyPresence.isPresent()) SandFilter.register();
    }
}
