package com.thirstwastaken2.createfly;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Whether the Create installed is the Create Fly port the Sand Filter was compiled against.
 *
 * <p>Names no Create class, so the entrypoints and the mixin plugin can ask before anything that
 * extends one is loaded. It names no Minecraft class either, not even through the mod's own logger:
 * the mixin plugin asks before Minecraft's classes may be loaded.
 */
public final class CreateFlyPresence {
    public static final String MOD_ID = "create";
    /** A class only Create Fly has. Read as a resource, so asking does not load it. */
    private static final String MARKER = "com/zurrtum/create/foundation/blockEntity/SmartBlockEntity.class";

    private static final Logger LOGGER = LoggerFactory.getLogger("thirstwastaken2");

    private static volatile Boolean present;

    private CreateFlyPresence() { }

    /**
     * Another mod may claim the {@code create} id, and a later Create Fly may move the classes the
     * integration extends, so the id alone is not enough.
     */
    public static boolean isPresent() {
        Boolean known = present;
        if (known == null) {
            known = FabricLoader.getInstance().isModLoaded(MOD_ID)
                    && CreateFlyPresence.class.getClassLoader().getResource(MARKER) != null;
            if (FabricLoader.getInstance().isModLoaded(MOD_ID) && !known) {
                LOGGER.warn("A mod with id 'create' is installed but is not Create Fly; "
                        + "the Sand Filter is disabled");
            }
            present = known;
        }
        return known;
    }
}
