package com.thirstwastaken2.client.compat;

import com.thirstwastaken2.compat.AppleSkin;
import squeek.appleskin.ModConfig;

/**
 * Reads AppleSkin's own settings, which means naming its classes, so it stays out of the main HUD
 * class and is only touched once {@link AppleSkin#isLoaded()} holds. Whether AppleSkin is there at
 * all, and the settings the mod keeps for it, are in {@link AppleSkin}.
 */
public final class AppleSkinIntegration {
    private AppleSkinIntegration() { }

    /** Mirrors AppleSkin's own exhaustion-underlay toggle for the thirst bar. */
    public static boolean shouldShowExhaustion() {
        if (!AppleSkin.isLoaded()) return false;
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
