package com.thirstwastaken2.create;

import com.thirstwastaken2.ThirstWasTaken2;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * A second mod class for the same mod id, compiled only on the nodes that set {@code deps.create}. FML
 * constructs every {@code @Mod} class of a mod, so the loader's own entrypoint never has to name this
 * one. It references {@link SandFilter} only after the presence check, so the classes that extend
 * Create's are never loaded without Create.
 */
@Mod(ThirstWasTaken2.MOD_ID)
public final class CreateEntrypoint {
    public CreateEntrypoint(IEventBus modBus) {
        if (CreatePresence.isPresent()) SandFilter.register(modBus);
    }
}
