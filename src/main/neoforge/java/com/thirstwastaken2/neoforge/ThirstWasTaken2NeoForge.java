package com.thirstwastaken2.neoforge;

import com.thirstwastaken2.ThirstWasTaken2;
import net.neoforged.fml.common.Mod;

/** The NeoForge mod class. Everything it starts is loader independent. */
@Mod(ThirstWasTaken2.MOD_ID)
public final class ThirstWasTaken2NeoForge {
    public ThirstWasTaken2NeoForge() {
        ThirstWasTaken2.initialize();
    }
}
