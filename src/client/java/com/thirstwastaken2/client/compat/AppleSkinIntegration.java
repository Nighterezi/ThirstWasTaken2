package com.thirstwastaken2.client.compat;

import net.fabricmc.loader.api.FabricLoader;
import squeek.appleskin.ModConfig;

/** Optional AppleSkin hooks kept outside the main HUD class so the mod remains safe without it. */
public final class AppleSkinIntegration {
    private static final boolean LOADED = FabricLoader.getInstance().isModLoaded("appleskin");

    private AppleSkinIntegration() { }

    /** Mirrors AppleSkin's own exhaustion-underlay toggle for the thirst bar. */
    public static boolean shouldShowExhaustion() {
        if (!LOADED) return false;
        return AppleSkinConfig.shouldShowExhaustion();
    }

    /** Loaded only after Fabric Loader confirms AppleSkin is present. */
    private static final class AppleSkinConfig {
        private static boolean shouldShowExhaustion() {
            ModConfig config = ModConfig.INSTANCE;
            return config != null && config.showFoodExhaustionHudUnderlay;
        }
    }
}
