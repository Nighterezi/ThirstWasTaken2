package com.thirstwastaken2.sereneseasons.platform;

import net.minecraft.world.level.Level;
//? if >=26.2 {
import sereneseasons.api.season.SeasonHelper;
//?} else {
/*import sereneseasons.init.ModConfig;
*///?}

/** The Serene Seasons calls whose shape differs between its builds. */
public final class SeasonsPlatform {
    private SeasonsPlatform() { }

    /**
     * Whether Serene Seasons' config gives {@code level}'s dimension seasons; by default only the
     * Overworld. The API asks from 26.2; before it, the config it reads is the only place to ask.
     */
    public static boolean hasSeasons(Level level) {
        //? if >=26.2 {
        return SeasonHelper.hasSeasons(level);
        //?} else {
        /*return ModConfig.seasons.isDimensionWhitelisted(level.dimension());
        *///?}
    }
}
