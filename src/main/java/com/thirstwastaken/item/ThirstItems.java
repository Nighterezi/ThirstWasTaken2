package com.thirstwastaken.item;

import com.thirstwastaken.ThirstWasTaken;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.component.Consumables;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

import java.util.function.Function;

public final class ThirstItems {
    public static final Item CLAY_BOWL = register("clay_bowl", Item::new, new Item.Properties().stacksTo(64));
    public static final Item TERRACOTTA_BOWL = register("terracotta_bowl", Item::new, new Item.Properties().stacksTo(64));
    public static final Item TERRACOTTA_WATER_BOWL = register("terracotta_water_bowl", Item::new,
            new Item.Properties().stacksTo(64).usingConvertsTo(TERRACOTTA_BOWL)
                    .component(net.minecraft.core.component.DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK));

    private ThirstItems() { }

    private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ThirstWasTaken.id(name));
        Item item = factory.apply(properties.setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static void register() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
            entries.accept(CLAY_BOWL);
            entries.accept(TERRACOTTA_BOWL);
            entries.accept(TERRACOTTA_WATER_BOWL);
        });
    }
}
