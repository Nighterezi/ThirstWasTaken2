package com.thirstwastaken2.fabric;

import com.thirstwastaken2.ThirstWasTaken2;
import net.fabricmc.api.ModInitializer;

/** The Fabric {@code main} entrypoint. Everything it starts is loader independent. */
public final class ThirstWasTaken2Fabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ThirstWasTaken2.initialize();
    }
}
