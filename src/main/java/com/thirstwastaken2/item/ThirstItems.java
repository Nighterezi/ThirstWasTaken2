package com.thirstwastaken2.item;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.platform.DrinkItem;
import com.thirstwastaken2.platform.Loader;
import com.thirstwastaken2.platform.Vanilla;
import com.thirstwastaken2.purity.ThirstComponents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ThirstItems {
    /** Custom model data index the filled bowl's sprite dispatches on: its grade, with salt one past. */
    public static final int BOWL_MODEL_INDEX = 1;
    /** Custom model data index the waterskin's sprite dispatches on: servings left. */
    public static final int WATERSKIN_MODEL_INDEX = 0;

    public static final Item CLAY_BOWL = Vanilla.registerItem("clay_bowl", Item::new, new Item.Properties().stacksTo(64));
    public static final Item TERRACOTTA_BOWL = Vanilla.registerItem("terracotta_bowl", Item::new, new Item.Properties().stacksTo(64));
    /**
     * A filled bowl does not stack, matching every vanilla drink container. Stacking it would put
     * dozens of drinks in one slot and leave the waterskin, which holds three, with no purpose.
     */
    public static final Item TERRACOTTA_WATER_BOWL = Vanilla.registerItem("terracotta_water_bowl",
            properties -> new DrinkItem(properties, TERRACOTTA_BOWL),
            new Item.Properties().stacksTo(1)
                    .component(ThirstComponents.WATER_PURITY, 3)
                    .component(ThirstComponents.WATER_SALTY, false)
                    .component(DataComponents.CUSTOM_MODEL_DATA, Vanilla.modelSelector(BOWL_MODEL_INDEX, 3)));
    public static final Item WATERSKIN = Vanilla.registerItem("waterskin", WaterskinItem::new,
            new Item.Properties().stacksTo(1)
                    .component(ThirstComponents.WATER_SERVINGS, 0));
    public static final ResourceKey<CreativeModeTab> CREATIVE_TAB_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, ThirstWasTaken2.id("thirstwastaken2"));

    private ThirstItems() { }

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CREATIVE_TAB_KEY,
                Loader.creativeTabBuilder()
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
