package com.thirstwastaken2.sereneseasons;

import com.thirstwastaken2.platform.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Whether the Serene Seasons installed is one the integration was written against.
 *
 * <p>Names no Serene Seasons class, so the entrypoint can ask before anything of Serene Seasons' is
 * loaded. The API's two classes are looked up as resources, which never loads them. Both loaders
 * compile this directory, so the mod list is asked through the loader seam.
 */
public final class SereneSeasonsPresence {
    public static final String MOD_ID = "sereneseasons";
    /** The API the climate reads: the calendar and the per-level state it returns. */
    private static final String[] MARKERS = {
            "sereneseasons/api/season/SeasonHelper.class",
            "sereneseasons/api/season/ISeasonState.class",
    };

    private static final Logger LOGGER = LoggerFactory.getLogger("thirstwastaken2");

    private static volatile Boolean present;

    private SereneSeasonsPresence() { }

    /**
     * Another mod may claim the {@code sereneseasons} id, and a later Serene Seasons may move its API, so
     * the id alone is not enough.
     */
    public static boolean isPresent() {
        Boolean known = present;
        if (known == null) {
            boolean loaded = Loader.isModLoaded(MOD_ID);
            known = loaded;
            for (String marker : MARKERS) {
                if (known && SereneSeasonsPresence.class.getClassLoader().getResource(marker) == null) known = false;
            }
            if (loaded && !known) {
                LOGGER.warn("A mod with id 'sereneseasons' is installed but has no season API where Serene Seasons "
                        + "keeps it; thirst will not follow the seasons");
            }
            present = known;
        }
        return known;
    }
}
