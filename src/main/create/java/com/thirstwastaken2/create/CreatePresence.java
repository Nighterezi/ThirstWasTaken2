package com.thirstwastaken2.create;

import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;

/**
 * Whether the Create installed is the one the Sand Filter was compiled against.
 *
 * <p>Names no Create class and no Minecraft class, so the entrypoint and the mixin plugin can ask
 * before anything that extends one is loaded. It reads FML's list of discovered mod files, which is
 * complete before any mixin config is read.
 */
public final class CreatePresence {
    public static final String MOD_ID = "create";
    /** A class Create 6 has. Looked up in Create's own jar, so asking does not load it. */
    private static final String MARKER = "com/simibubi/create/foundation/blockEntity/SmartBlockEntity.class";

    private static final Logger LOGGER = LoggerFactory.getLogger("thirstwastaken2");

    private static volatile Boolean present;

    private CreatePresence() { }

    /**
     * Another mod may claim the {@code create} id, and a later Create may move the classes the
     * integration extends, so the id alone is not enough.
     */
    public static boolean isPresent() {
        Boolean known = present;
        if (known == null) {
            ModFileInfo create = LoadingModList.get().getModFileById(MOD_ID);
            known = create != null && Files.exists(create.getFile().findResource(MARKER));
            if (create != null && !known) {
                LOGGER.warn("A mod with id 'create' is installed but is not Create 6; the Sand Filter is disabled");
            }
            present = known;
        }
        return known;
    }
}
