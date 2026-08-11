package com.thirstwastaken2.item;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.purity.ThirstComponents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumables;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;

import java.util.function.Function;

public final class ThirstItems {
    public static final Item CLAY_BOWL = register("clay_bowl", Item::new, new Item.Properties().stacksTo(64));
    public static final Item TERRACOTTA_BOWL = register("terracotta_bowl", Item::new, new Item.Properties().stacksTo(64));
    public static final Item TERRACOTTA_WATER_BOWL = register("terracotta_water_bowl", Item::new,
            new Item.Properties().stacksTo(64).usingConvertsTo(TERRACOTTA_BOWL)
                    .component(ThirstComponents.WATER_PURITY, 0)
                    .component(net.minecraft.core.component.DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK));
    public static final Item WATERSKIN = register("waterskin", WaterskinItem::new,
            new Item.Properties().stacksTo(1)
                    .component(ThirstComponents.WATER_SERVINGS, 0)
                    .component(net.minecraft.core.component.DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK));
    public static final ResourceKey<CreativeModeTab> CREATIVE_TAB_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, ThirstWasTaken2.id("thirstwastaken2"));

    private ThirstItems() { }

    private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ThirstWasTaken2.id(name));
        Item item = factory.apply(properties.setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CREATIVE_TAB_KEY,
                FabricCreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.thirstwastaken2"))
                        .icon(() -> new ItemStack(TERRACOTTA_WATER_BOWL))
                        .displayItems((parameters, entries) -> {
                            entries.accept(CLAY_BOWL);
                            entries.accept(TERRACOTTA_BOWL);
                            entries.accept(TERRACOTTA_WATER_BOWL);
                            entries.accept(WATERSKIN);
                        })
                        .build());
    }
}
