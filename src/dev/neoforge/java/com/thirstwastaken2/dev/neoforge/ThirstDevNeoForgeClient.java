package com.thirstwastaken2.dev.neoforge;

import com.thirstwastaken2.dev.ThirstDev;
import com.thirstwastaken2.dev.ThirstDevClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

/**
 * The dev tools mod on NeoForge, client side: the agent's queue, polled on the client tick, and
 * {@code /thirst benchmark} on the integrated server, the same pair a Fabric client gets.
 */
@Mod(value = ThirstDevNeoForge.MOD_ID, dist = Dist.CLIENT)
public final class ThirstDevNeoForgeClient {
    public ThirstDevNeoForgeClient() {
        ThirstDevClient.initialize();
        ThirstDev.installBenchmark();
    }
}
