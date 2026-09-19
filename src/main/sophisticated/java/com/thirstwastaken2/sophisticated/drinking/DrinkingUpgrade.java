package com.thirstwastaken2.sophisticated.drinking;

import com.mojang.serialization.Codec;
import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.purity.WaterPurity;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerRegistry;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerType;

/**
 * The Drinking and Advanced Drinking upgrades: their items, the two settings they keep on the upgrade
 * stack, and their settings containers. Only registered alongside Sophisticated Core.
 */
public final class DrinkingUpgrade {
    public static final String NAME = "drinking_upgrade";
    public static final String ADVANCED_NAME = "advanced_drinking_upgrade";

    /** The same filter sizes as the Feeding upgrades, whose settings these mirror. */
    public static final int FILTER_SLOTS = 9;
    public static final int FILTER_SLOTS_IN_ROW = 3;
    public static final int ADVANCED_FILTER_SLOTS = 16;
    public static final int ADVANCED_FILTER_SLOTS_IN_ROW = 4;

    public static final DataComponentType<DrinkAt> DRINK_AT = DataComponentType.<DrinkAt>builder()
            .persistent(DrinkAt.CODEC)
            .networkSynchronized(DrinkAt.STREAM_CODEC)
            .build();

    /** The lowest grade of fresh water the upgrade drinks on its own. Salt water never is. */
    public static final DataComponentType<Integer> MIN_PURITY = DataComponentType.<Integer>builder()
            .persistent(Codec.intRange(WaterPurity.MIN, WaterPurity.MAX))
            .networkSynchronized(ByteBufCodecs.VAR_INT)
            .build();

    public static final UpgradeContainerType<DrinkingUpgradeWrapper, DrinkingUpgradeContainer> TYPE =
            new UpgradeContainerType<>(DrinkingUpgradeContainer::new);
    public static final UpgradeContainerType<DrinkingUpgradeWrapper, DrinkingUpgradeContainer> ADVANCED_TYPE =
            new UpgradeContainerType<>(DrinkingUpgradeContainer::new);

    private static DrinkingUpgradeItem item;
    private static DrinkingUpgradeItem advancedItem;

    private DrinkingUpgrade() { }

    public static void register(IEventBus modBus) {
        modBus.addListener(DrinkingUpgrade::onRegister);
        modBus.addListener(DrinkingUpgrade::onCreativeTab);
    }

    private static void onRegister(RegisterEvent event) {
        event.register(Registries.DATA_COMPONENT_TYPE, helper -> {
            helper.register(ThirstWasTaken2.id("drink_at"), DRINK_AT);
            helper.register(ThirstWasTaken2.id("drink_min_purity"), MIN_PURITY);
        });
        event.register(Registries.ITEM, helper -> {
            item = new DrinkingUpgradeItem(FILTER_SLOTS, false);
            advancedItem = new DrinkingUpgradeItem(ADVANCED_FILTER_SLOTS, true);
            helper.register(ThirstWasTaken2.id(NAME), item);
            helper.register(ThirstWasTaken2.id(ADVANCED_NAME), advancedItem);
        });
        // Sophisticated Core registers its own upgrades' containers at the same point.
        event.register(Registries.MENU, helper -> {
            UpgradeContainerRegistry.register(ThirstWasTaken2.id(NAME), TYPE);
            UpgradeContainerRegistry.register(ThirstWasTaken2.id(ADVANCED_NAME), ADVANCED_TYPE);
            ThirstWasTaken2.LOGGER.info("Sophisticated Core found, registered the Drinking upgrades");
        });
    }

    private static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(ThirstItems.CREATIVE_TAB_KEY)) return;
        event.accept(item);
        event.accept(advancedItem);
    }
}
