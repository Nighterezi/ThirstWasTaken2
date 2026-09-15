package com.thirstwastaken2.client.compat;

import com.thirstwastaken2.client.platform.ClientLoader;
import com.thirstwastaken2.compat.AppleSkin;
import com.thirstwastaken2.config.QuenchedOverlay;

/**
 * Decides whether to ask for AppleSkin's own settings, which only happens once {@link AppleSkin#isLoaded()}
 * holds. The read itself is behind {@link ClientLoader}, because AppleSkin keeps its config differently
 * on each loader. Whether AppleSkin is there at all, and the settings the mod keeps for it, are in
 * {@link AppleSkin}.
 */
public final class AppleSkinIntegration {
    private AppleSkinIntegration() { }

    /**
     * Whether the thirst bar gets the exhaustion strip: AppleSkin's own exhaustion-underlay toggle,
     * unless the quenched outline is off. Off turns the AppleSkin look off the thirst bar as a whole.
     */
    public static boolean shouldShowExhaustion() {
        if (!AppleSkin.isLoaded() || AppleSkin.quenchedOverlay() == QuenchedOverlay.OFF) return false;
        return ClientLoader.appleSkinShowsExhaustionUnderlay();
    }
}
