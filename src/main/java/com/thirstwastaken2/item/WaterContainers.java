package com.thirstwastaken2.item;

import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.minecraft.world.item.ItemStack;

/**
 * The mod's own containers counted in servings, for the loaders' fluid APIs: the waterskin holds three
 * and the terracotta bowl one. Each loader turns a serving into its own fluid unit, a bottle's 250 mB,
 * and keeps the rules its APIs share on top of this.
 */
public final class WaterContainers {
    private WaterContainers() { }

    public static boolean handles(ItemStack stack) {
        return stack.is(ThirstItems.WATERSKIN) || stack.is(ThirstItems.TERRACOTTA_BOWL)
                || stack.is(ThirstItems.TERRACOTTA_WATER_BOWL);
    }

    public static int capacity(ItemStack stack) {
        return stack.is(ThirstItems.WATERSKIN) ? WaterskinItem.CAPACITY : 1;
    }

    public static int servings(ItemStack stack) {
        if (stack.is(ThirstItems.WATERSKIN)) return WaterskinItem.servings(stack);
        return stack.is(ThirstItems.TERRACOTTA_WATER_BOWL) ? 1 : 0;
    }

    /**
     * One {@code container} holding {@code servings} of water of {@code quality}, or {@code null} when it
     * cannot hold that many. The quality is only read when the container has to gain water.
     */
    public static ItemStack holding(ItemStack container, WaterQuality quality, int servings) {
        if (servings < 0 || servings > capacity(container)) return null;
        if (container.is(ThirstItems.WATERSKIN)) {
            ItemStack skin = container.copyWithCount(1);
            int held = WaterskinItem.servings(skin);
            if (servings > held) WaterskinItem.addWater(skin, quality, servings - held);
            else if (servings < held) WaterskinItem.removeWater(skin, held - servings);
            return skin;
        }
        if (servings == 0) return new ItemStack(ThirstItems.TERRACOTTA_BOWL);
        return WaterPurity.setQuality(new ItemStack(ThirstItems.TERRACOTTA_WATER_BOWL), quality);
    }
}
