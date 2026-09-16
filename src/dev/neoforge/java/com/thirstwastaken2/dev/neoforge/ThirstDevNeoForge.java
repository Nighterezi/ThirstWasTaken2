package com.thirstwastaken2.dev.neoforge;

import com.thirstwastaken2.dev.ThirstDev;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

/**
 * The dev tools mod on NeoForge, dedicated server side: the agent's queue, polled on the server tick.
 *
 * <p>The mod is declared in {@code META-INF/neoforge.mods.toml} and has one class per side rather than
 * one class that asks which side it is on, which is the same shape the mod itself uses. The benchmark
 * is not here: it simulates players with Fabric's {@code FakePlayer} and stays on that loader, as
 * {@code docs/dev/AGENT-CLIENT-PLAN.md} records.
 */
@Mod(value = ThirstDevNeoForge.MOD_ID, dist = Dist.DEDICATED_SERVER)
public final class ThirstDevNeoForge {
    /** NeoForge mod ids cannot contain a hyphen, so this is not the Fabric mod's {@code -dev} spelling. */
    public static final String MOD_ID = "thirstwastaken2_dev";

    public ThirstDevNeoForge() {
        ThirstDev.initializeServer();
    }
}
