package com.thirstwastaken2.sophisticated;

import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;

/**
 * Whether the Sophisticated Core installed is one the integration was compiled against.
 *
 * <p>Names no Sophisticated class and no Minecraft class, so the mixin plugin can ask before anything
 * is loaded. It reads FML's list of discovered mod files, which is complete before any mixin config is
 * read.
 */
public final class SophisticatedPresence {
    public static final String MOD_ID = "sophisticatedcore";
    /**
     * A class only the {@code IFluidHandler} generation of Sophisticated Core has: from 1.21.11 its tanks
     * move fluid through NeoForge's transfer API instead. Looked up in the mod's own jar, so asking does
     * not load it.
     */
    private static final String MARKER = "net/p3pp3rf1y/sophisticatedcore/upgrades/tank/TankUpgradeWrapper$SwapEmptyFluidContainerHandler.class";

    private static final Logger LOGGER = LoggerFactory.getLogger("thirstwastaken2");

    private static volatile Boolean present;

    private SophisticatedPresence() { }

    public static boolean isPresent() {
        Boolean known = present;
        if (known == null) {
            ModFileInfo core = LoadingModList.get().getModFileById(MOD_ID);
            known = core != null && Files.exists(core.getFile().findResource(MARKER));
            if (core != null && !known) {
                LOGGER.warn("Sophisticated Core is installed but is not a version ThirstWasTaken2 supports; "
                        + "water quality through its upgrades is disabled");
            }
            present = known;
        }
        return known;
    }
}
