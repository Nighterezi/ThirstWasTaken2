package com.thirstwastaken2.sophisticated;

import com.thirstwastaken2.sophisticated.platform.ModFiles;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Whether the Sophisticated Core installed is one the integration was compiled against.
 *
 * <p>Names no Sophisticated class and no Minecraft class, so the mixin plugin can ask before anything
 * is loaded. It reads FML's list of discovered mod files, which is complete before any mixin config is
 * read.
 */
public final class SophisticatedPresence {
    public static final String MOD_ID = "sophisticatedcore";

    private static final Logger LOGGER = LoggerFactory.getLogger("thirstwastaken2");

    private static volatile Boolean present;

    private SophisticatedPresence() { }

    public static boolean isPresent() {
        Boolean known = present;
        if (known == null) {
            ModFileInfo core = LoadingModList.get().getModFileById(MOD_ID);
            // A class only the generation of Core this node's fluid code targets has, IFluidHandler on
            // 1.21.1 or the transfer API from 1.21.11. Looked up in the mod's own jar, so asking does not
            // load it.
            known = core != null && ModFiles.contains(core, SophisticatedGeneration.MARKER);
            if (core != null && !known) {
                LOGGER.warn("Sophisticated Core is installed but is not a version ThirstWasTaken2 supports; "
                        + "water quality through its upgrades is disabled");
            }
            present = known;
        }
        return known;
    }
}
