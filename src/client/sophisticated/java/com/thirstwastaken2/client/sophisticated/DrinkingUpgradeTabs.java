package com.thirstwastaken2.client.sophisticated;

import com.thirstwastaken2.sophisticated.drinking.DrinkingUpgrade;
import com.thirstwastaken2.sophisticated.drinking.DrinkingUpgradeContainer;
import com.thirstwastaken2.sophisticated.drinking.DrinkingUpgradeWrapper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.p3pp3rf1y.sophisticatedcore.client.gui.UpgradeGuiManager;

/**
 * The Drinking upgrades' settings tabs, registered where Sophisticated Backpacks registers its own, on
 * the main thread, since the tab map is a plain one.
 *
 * <p>Apart from {@code SophisticatedClientEntrypoint} for the reason {@code SandFilterClient} is apart
 * from its own entrypoint: every method here names a Sophisticated class, and the JVM verifies a class
 * whole when it is linked, before any code in it runs. Left in the entrypoint, the tab factories made
 * the verifier load {@link DrinkingUpgradeTab} and with it its Sophisticated superclass, which crashed
 * the client without Sophisticated Core however early the entrypoint asked the gate.
 */
final class DrinkingUpgradeTabs {
    private DrinkingUpgradeTabs() { }

    static void register(IEventBus modBus) {
        modBus.addListener(DrinkingUpgradeTabs::onMenuScreens);
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
