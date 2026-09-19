package com.thirstwastaken2.client.sophisticated;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.sophisticated.SophisticatedPresence;
import com.thirstwastaken2.sophisticated.drinking.DrinkingUpgrade;
import com.thirstwastaken2.sophisticated.drinking.DrinkingUpgradeContainer;
import com.thirstwastaken2.sophisticated.drinking.DrinkingUpgradeWrapper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.p3pp3rf1y.sophisticatedcore.client.gui.UpgradeGuiManager;

/**
 * The client half of {@code SophisticatedEntrypoint}: the Drinking upgrades' settings tabs. Registered
 * where Sophisticated Backpacks registers its own, on the main thread, since the tab map is a plain one.
 */
@Mod(value = ThirstWasTaken2.MOD_ID, dist = Dist.CLIENT)
public final class SophisticatedClientEntrypoint {
    public SophisticatedClientEntrypoint(IEventBus modBus) {
        if (SophisticatedPresence.isPresent()) modBus.addListener(SophisticatedClientEntrypoint::onMenuScreens);
    }

    private static void onMenuScreens(RegisterMenuScreensEvent event) {
        UpgradeGuiManager.<DrinkingUpgradeWrapper, DrinkingUpgradeContainer, DrinkingUpgradeTab>registerTab(DrinkingUpgrade.TYPE,
                (container, position, screen) ->
                        new DrinkingUpgradeTab.Basic(container, position, screen, DrinkingUpgrade.FILTER_SLOTS_IN_ROW));
        UpgradeGuiManager.<DrinkingUpgradeWrapper, DrinkingUpgradeContainer, DrinkingUpgradeTab>registerTab(DrinkingUpgrade.ADVANCED_TYPE,
                (container, position, screen) ->
                        new DrinkingUpgradeTab.Advanced(container, position, screen, DrinkingUpgrade.ADVANCED_FILTER_SLOTS_IN_ROW));
    }
}
