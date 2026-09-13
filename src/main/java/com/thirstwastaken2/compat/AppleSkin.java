package com.thirstwastaken2.compat;

import com.thirstwastaken2.config.QuenchedOverlay;
import com.thirstwastaken2.config.ThirstConfig;
import com.thirstwastaken2.platform.Loader;

/**
 * What the mod only shows alongside AppleSkin: the quenched outline on the thirst bar and the droplet
 * rows in item tooltips. They are the thirst half of what AppleSkin adds for hunger, so without it
 * the thirst bar looks like vanilla's food bar does, and tooltips say nothing about restoring either.
 *
 * <p>Only the presence check lives here, so common code can ask it without touching an AppleSkin
 * class. Reading AppleSkin's own settings is client code, in {@code client/compat/AppleSkinIntegration}.
 */
public final class AppleSkin {
    private static final boolean LOADED = Loader.isModLoaded("appleskin");

    private AppleSkin() { }

    public static boolean isLoaded() {
        return LOADED;
    }

    /** The outline to draw over the thirst bar, {@link QuenchedOverlay#OFF} without AppleSkin. */
    public static QuenchedOverlay quenchedOverlay() {
        return LOADED ? ThirstConfig.get().appleskinQuenchedOverlay : QuenchedOverlay.OFF;
    }

    /** Whether item tooltips get the thirst and quenched droplet rows. */
    public static boolean showsTooltipDroplets() {
        return LOADED && ThirstConfig.get().appleskinTooltipDroplets;
    }
}
