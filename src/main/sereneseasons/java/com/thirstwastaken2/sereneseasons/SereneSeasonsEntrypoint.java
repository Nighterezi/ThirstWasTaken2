package com.thirstwastaken2.sereneseasons;

import com.thirstwastaken2.platform.IntegrationEntrypoint;

/**
 * Run once the mod has initialized, on both loaders: Fabric through the {@code thirstwastaken2:integration}
 * entrypoint, NeoForge by the annotation. It names no Serene Seasons class and hands over to
 * {@link SereneSeasonsClimate} only after the presence check, so nothing of Serene Seasons' is loaded
 * without it.
 */
@IntegrationEntrypoint
public final class SereneSeasonsEntrypoint implements Runnable {
    @Override
    public void run() {
        if (SereneSeasonsPresence.isPresent()) SereneSeasonsClimate.install();
    }
}
