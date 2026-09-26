package com.thirstwastaken2.coldsweat;

import com.thirstwastaken2.ThirstWasTaken2;
import net.neoforged.fml.common.Mod;

/**
 * A second mod class for the same mod id, compiled only on the nodes that set {@code deps.cold_sweat}.
 * FML constructs every {@code @Mod} class of a mod, so the loader's own entrypoint never has to name
 * this one. It names no Cold Sweat class and hands over to {@link ColdSweatClimate} only after the
 * presence check, so nothing of Cold Sweat's is loaded without it.
 */
@Mod(ThirstWasTaken2.MOD_ID)
public final class ColdSweatEntrypoint {
    public ColdSweatEntrypoint() {
        if (ColdSweatPresence.isPresent()) ColdSweatClimate.install();
    }
}
