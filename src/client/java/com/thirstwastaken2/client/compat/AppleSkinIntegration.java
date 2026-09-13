package com.thirstwastaken2.client.compat;

import com.thirstwastaken2.compat.AppleSkin;
import com.thirstwastaken2.config.QuenchedOverlay;
import squeek.appleskin.ModConfig;

/**
 * Reads AppleSkin's own settings, which means naming its classes, so it stays out of the main HUD
 * class and is only touched once {@link AppleSkin#isLoaded()} holds. Whether AppleSkin is there at
 * all, and the settings the mod keeps for it, are in {@link AppleSkin}.
 */
public final class AppleSkinIntegration {
    private AppleSkinIntegration() { }

    /**
     * Whether the thirst bar gets the exhaustion strip: AppleSkin's own exhaustion-underlay toggle,
     * unless the quenched outline is off. Off turns the AppleSkin look off the thirst bar as a whole.
     */
    public static boolean shouldShowExhaustion() {
        if (!AppleSkin.isLoaded() || AppleSkin.quenchedOverlay() == QuenchedOverlay.OFF) return false;
        return AppleSkinConfig.shouldShowExhaustion();
    }

    /** Loaded only after the loader confirms AppleSkin is present. */
    private static final class AppleSkinConfig {
        private static boolean shouldShowExhaustion() {
            ModConfig config = ModConfig.INSTANCE;
            return config != null && config.showFoodExhaustionHudUnderlay;
        }
    }
}
