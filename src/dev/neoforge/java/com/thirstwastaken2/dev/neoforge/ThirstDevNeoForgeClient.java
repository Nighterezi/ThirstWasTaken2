package com.thirstwastaken2.dev.neoforge;

import com.thirstwastaken2.dev.ThirstDevClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

/** The dev tools mod on NeoForge, client side: the agent's queue, polled on the client tick. */
@Mod(value = ThirstDevNeoForge.MOD_ID, dist = Dist.CLIENT)
public final class ThirstDevNeoForgeClient {
    public ThirstDevNeoForgeClient() {
        ThirstDevClient.initialize();
    }
}
