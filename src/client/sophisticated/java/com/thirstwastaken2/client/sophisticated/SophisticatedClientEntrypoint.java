package com.thirstwastaken2.client.sophisticated;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.sophisticated.SophisticatedPresence;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * The client half of {@code SophisticatedEntrypoint}: the Drinking upgrades' settings tabs, in
 * {@link DrinkingUpgradeTabs}. FML loads this class on every client, so it names no Sophisticated class
 * itself; the tabs are a call away, past the gate.
 */
@Mod(value = ThirstWasTaken2.MOD_ID, dist = Dist.CLIENT)
public final class SophisticatedClientEntrypoint {
    public SophisticatedClientEntrypoint(IEventBus modBus) {
        if (SophisticatedPresence.isPresent()) DrinkingUpgradeTabs.register(modBus);
    }
}
