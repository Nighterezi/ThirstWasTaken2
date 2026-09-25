package com.thirstwastaken2.client.brewinandchewin;

import com.thirstwastaken2.brewinandchewin.BrewinAndChewinPresence;
import com.thirstwastaken2.brewinandchewin.HeldKegWater;
import com.thirstwastaken2.client.compat.JadeIntegration;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Tells the mod's Jade plugin how to read the water in a Brewin' and Chewin' keg, which keeps its grade
 * on the tank's fluid rather than in a blockstate. Brewin' and Chewin' has no Jade plugin of its own to
 * add to. The overlay and its one entry in Jade's settings stay {@code client/compat/JadeIntegration}'s.
 *
 * <p>Jade loads this whether or not Brewin' and Chewin' is installed, so it names no class of the mod's:
 * {@link HeldKegWater} is this mod's own interface, which the keg only carries once its mixin is
 * applied. The gate is asked anyway, so nothing is added where the keg is not there.
 */
@WailaPlugin
public final class BrewinAndChewinJade implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        if (!BrewinAndChewinPresence.hasTarget(BrewinAndChewinPresence.KEG)) return;
        JadeIntegration.addContainer(blockEntity -> blockEntity instanceof HeldKegWater keg ? keg.thirst$heldWater() : null);
    }
}
