package com.thirstwastaken2.sophisticated;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.sophisticated.drinking.DrinkingUpgrade;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * A mod class of its own for the content the integration registers, compiled only on the nodes that set
 * {@code deps.sophisticated_core}, the way {@code CreateEntrypoint} is for Create. It references
 * {@link DrinkingUpgrade} only after the presence check, so nothing that extends a Sophisticated class
 * is loaded without Sophisticated Core.
 */
@Mod(ThirstWasTaken2.MOD_ID)
public final class SophisticatedEntrypoint {
    public SophisticatedEntrypoint(IEventBus modBus) {
        if (SophisticatedPresence.isPresent()) DrinkingUpgrade.register(modBus);
    }
}
