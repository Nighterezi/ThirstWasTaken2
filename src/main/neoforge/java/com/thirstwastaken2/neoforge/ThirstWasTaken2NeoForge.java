package com.thirstwastaken2.neoforge;

import com.thirstwastaken2.ThirstWasTaken2;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * The NeoForge mod class. Everything it starts is loader independent, apart from the fluid capability
 * that lets other mods fill and empty the waterskin and the bowls.
 */
@Mod(ThirstWasTaken2.MOD_ID)
public final class ThirstWasTaken2NeoForge {
    public ThirstWasTaken2NeoForge(IEventBus modBus) {
        ThirstWasTaken2.initialize();
        modBus.addListener(WaterContainerCapabilities::register);
    }
}
