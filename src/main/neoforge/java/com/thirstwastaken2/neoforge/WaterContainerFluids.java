package com.thirstwastaken2.neoforge;

import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.item.WaterskinItem;
import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * The mod's own containers seen as fluid containers, so pipes, tanks and pumps of other mods can fill
 * and empty them: the waterskin holds three servings and the terracotta bowl one, a serving being a
 * bottle's 250 mB.
 *
 * <p>Only whole servings move, and a container that already holds water only takes more of the same
 * grade, the rule every tank keeps for two fluids with different components. Mixing grades is what
 * pouring by hand does, where the waterskin averages them; through a pipe it would be a second rule for
 * the same container.
 *
 * <p>Both of NeoForge's fluid APIs are built on this: {@code IFluidHandlerItem} on 1.21.1 and the
 * transfer API's {@code ResourceHandler} from 1.21.11. Each has its own source directory,
 * {@code src/main/neoforge-fluidhandler} and {@code src/main/neoforge-transfer}.
 */
public final class WaterContainerFluids {
    /** One drink, the size of a bottle. */
    public static final int SERVING = 250;

    private WaterContainerFluids() { }

    public static boolean handles(ItemStack stack) {
        return stack.is(ThirstItems.WATERSKIN) || stack.is(ThirstItems.TERRACOTTA_BOWL)
                || stack.is(ThirstItems.TERRACOTTA_WATER_BOWL);
    }

    /** Only still water, the fluid every water source and bucket holds. */
    public static boolean accepts(Fluid fluid) {
        return fluid == Fluids.WATER;
    }

    public static int capacity(ItemStack stack) {
        return stack.is(ThirstItems.WATERSKIN) ? WaterskinItem.CAPACITY * SERVING : SERVING;
    }

    /** {@code amount} rounded down to whole servings, so a request for 300 mB moves one. */
    public static int wholeServings(int amount) {
        return amount - amount % SERVING;
    }

    /** The water in one container, stamped with its grade, or empty. */
    public static FluidStack contents(ItemStack stack) {
        int servings = stack.is(ThirstItems.WATERSKIN) ? WaterskinItem.servings(stack)
                : stack.is(ThirstItems.TERRACOTTA_WATER_BOWL) ? 1 : 0;
        if (servings == 0) return FluidStack.EMPTY;
        return WaterFluids.stamp(new FluidStack(Fluids.WATER, servings * SERVING), WaterPurity.quality(stack));
    }

    /**
     * One {@code container} holding {@code amount} of water of {@code quality}, or {@code null} when it
     * cannot: part of a serving, or more than it holds. The quality is only read when the container has
     * to gain water.
     */
    public static ItemStack holding(ItemStack container, WaterQuality quality, int amount) {
        if (amount < 0 || amount % SERVING != 0 || amount > capacity(container)) return null;
        int servings = amount / SERVING;
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
