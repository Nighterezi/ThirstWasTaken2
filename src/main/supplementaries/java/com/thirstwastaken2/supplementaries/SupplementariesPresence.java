package com.thirstwastaken2.supplementaries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Whether Moonlight Lib and Supplementaries are installed, and are versions the integration was
 * written against.
 *
 * <p>Names no Moonlight class, no Supplementaries class and no Minecraft class, so the mixin plugin can
 * ask before anything a mixin targets is loaded: every answer here is a resource lookup, which never
 * loads a class. It names no loader either, since both loaders compile this directory, which is why it
 * probes the classpath rather than asking a mod list the way {@code SophisticatedPresence} does.
 *
 * <p>The two are asked separately. Moonlight carries the soft fluid system three of the mixins target
 * and ships inside other mods than Supplementaries; only the cauldron mixin needs Supplementaries
 * itself.
 */
public final class SupplementariesPresence {
    private static final String MOONLIGHT = "net/mehvahdjukaar/moonlight/core/Moonlight.class";
    /** The soft fluid tank, which every mixin but the cauldron one reaches. */
    private static final String SOFT_FLUIDS = "net/mehvahdjukaar/moonlight/api/fluids/SoftFluidTank.class";

    private static final String SUPPLEMENTARIES = "net/mehvahdjukaar/supplementaries/Supplementaries.class";
    /** The faucet behaviour for vanilla cauldrons, the one Supplementaries class the integration touches. */
    private static final String WATER_CAULDRON =
            "net/mehvahdjukaar/supplementaries/common/block/faucet/WaterCauldronInteraction.class";

    private static final Logger LOGGER = LoggerFactory.getLogger("thirstwastaken2");

    private static volatile Boolean moonlight;
    private static volatile Boolean supplementaries;

    private SupplementariesPresence() { }

    public static boolean hasMoonlight() {
        Boolean known = moonlight;
        if (known == null) {
            known = has(SOFT_FLUIDS);
            if (!known && has(MOONLIGHT)) {
                LOGGER.warn("Moonlight Lib is installed but is not a version ThirstWasTaken2 supports; "
                        + "water quality in jars, goblets and faucets is disabled");
            }
            moonlight = known;
        }
        return known;
    }

    public static boolean hasSupplementaries() {
        Boolean known = supplementaries;
        if (known == null) {
            known = hasMoonlight() && has(WATER_CAULDRON);
            if (!known && has(SUPPLEMENTARIES)) {
                LOGGER.warn("Supplementaries is installed but is not a version ThirstWasTaken2 supports; "
                        + "a faucet will not keep the grade of the water in a cauldron");
            }
            supplementaries = known;
        }
        return known;
    }

    private static boolean has(String resource) {
        return SupplementariesPresence.class.getClassLoader().getResource(resource) != null;
    }
}
