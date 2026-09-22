package com.thirstwastaken2.client.kaleidoscope;

import com.thirstwastaken2.client.compat.JadeIntegration;
import com.thirstwastaken2.kaleidoscope.BrewedWater;
import com.thirstwastaken2.kaleidoscope.KaleidoscopePresence;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Tells the mod's Jade plugin how to read the water in a Kaleidoscope Cookery stockpot or teapot, which
 * keep its grade on the block entity rather than in a blockstate. The overlay and its one entry in Jade's
 * settings stay {@code client/compat/JadeIntegration}'s.
 *
 * <p>Jade loads this whether or not Kaleidoscope Cookery is installed, so it names no class of the mod's:
 * {@link BrewedWater} is this mod's own interface, which the stockpot and the teapot only carry once
 * their mixins are applied. The gate is asked anyway, so nothing is added for a build the integration
 * does not support.
 */
@WailaPlugin
public final class KaleidoscopeJade implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        if (!KaleidoscopePresence.isSupported()) return;
        JadeIntegration.addContainer(blockEntity -> blockEntity instanceof BrewedWater water ? water.thirst$heldWater() : null);
    }
}
