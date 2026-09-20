package com.thirstwastaken2.client.supplementaries;

import com.thirstwastaken2.supplementaries.SupplementariesPresence;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Tells the mod's Jade plugin how to read the water out of a Supplementaries jar or goblet, which keep
 * it in a Moonlight soft fluid tank rather than in a blockstate. The overlay itself, and its one entry
 * in Jade's settings, stay that plugin's: {@code client/compat/JadeIntegration}, which is common client
 * code and may name no foreign class of its own.
 *
 * <p>This is the integration's only entry point, and Jade's is the right one to borrow: it is called on
 * a client exactly when there is a Jade to show anything.
 *
 * <p>Nothing here names Moonlight or Supplementaries. The gate is asked first, and only then is the
 * class that does name them loaded. Jade resolves this class through the {@code jade} entrypoint on
 * Fabric and through the annotation on NeoForge, either of which happens whether or not Supplementaries
 * is installed.
 */
@WailaPlugin
public final class SupplementariesJade implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        if (SupplementariesPresence.hasSupplementaries()) SoftFluidTooltip.register();
    }
}
