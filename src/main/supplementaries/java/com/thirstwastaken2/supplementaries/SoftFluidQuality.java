package com.thirstwastaken2.supplementaries;

import com.mojang.datafixers.util.Pair;
import com.thirstwastaken2.config.ThirstConfig;
import com.thirstwastaken2.purity.ThirstComponents;
import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.mehvahdjukaar.moonlight.api.fluids.FluidContainerList;
import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;

/**
 * Water quality on Moonlight's soft fluid stacks, the form water takes inside a Supplementaries jar,
 * goblet or faucet.
 *
 * <p>A soft fluid stack carries data components the way an item stack does, so it can hold the grade
 * itself. It keeps exactly one of the two, {@code water_purity} for a grade or {@code water_salty} for
 * sea water, so two stacks of the same water always compare equal and share a tank: Moonlight refuses
 * to mix stacks whose components differ, which is what keeps a jar of Murky water from quietly taking
 * Clean water. A container filled from a tank gets both components back, and its sprite, because
 * {@link WaterPurity#setQuality} stamps it on the way out.
 *
 * <p>This is {@code WaterFluids} for soft fluids, and it keeps the same rules for the same reasons.
 */
public final class SoftFluidQuality {
    private SoftFluidQuality() { }

    public static boolean isWater(SoftFluidStack stack) {
        return !stack.isEmpty() && stack.is(MLBuiltinSoftFluids.WATER);
    }

    /**
     * Asked of the registry entry rather than of a stack, for the one hook that has no stack. A soft
     * fluid names the vanilla fluids it stands for, and water's are the {@code c:water} tag.
     */
    public static boolean isWater(SoftFluid fluid) {
        return fluid.getVanillaFluid().value().isSame(Fluids.WATER);
    }

    /** Unstamped water, from a creative tank or a mod that knows nothing of grades, is the config default. */
    public static WaterQuality quality(SoftFluidStack stack) {
        if (Boolean.TRUE.equals(stack.get(ThirstComponents.WATER_SALTY))) return WaterQuality.SALT;
        Integer purity = stack.get(ThirstComponents.WATER_PURITY);
        return WaterQuality.fresh(purity != null ? purity : ThirstConfig.get().defaultPurity);
    }

    /** Writes {@code quality} onto water and returns the same stack. Anything else is left alone. */
    public static SoftFluidStack stamp(SoftFluidStack stack, WaterQuality quality) {
        if (!isWater(stack)) return stack;
        switch (quality) {
            case WaterQuality.Salt ignored -> {
                stack.getComponents().remove(ThirstComponents.WATER_PURITY);
                stack.set(ThirstComponents.WATER_SALTY, true);
            }
            case WaterQuality.Fresh fresh -> {
                stack.getComponents().remove(ThirstComponents.WATER_SALTY);
                stack.set(ThirstComponents.WATER_PURITY, fresh.purity());
            }
        }
        return stack;
    }

    /**
     * Gives water about to enter a tank the grade it already reads as, so that what a tank holds is
     * always stamped. Water that arrives without a grade would otherwise refuse to share a tank with
     * water that has one, although both read as {@code defaultPurity} to everything else.
     *
     * <p>Fresh water poured out of a container arrives carrying both components, since that is what the
     * mod writes on an item; this is also where it comes down to the one the tank compares on.
     */
    public static void normalise(SoftFluidStack stack) {
        if (isWater(stack)) stamp(stack, quality(stack));
    }

    /**
     * Stamps a container a tank has just filled. Moonlight builds it from scratch, so it goes through
     * {@link WaterPurity#setQuality} rather than keeping whatever was copied onto it: that is what
     * writes {@code water_salty: false} on fresh water, which every purification recipe matches on, and
     * what points a salty bottle at the sprite that says so.
     */
    public static Pair<ItemStack, FluidContainerList.Category> filled(
            Pair<ItemStack, FluidContainerList.Category> filled, WaterQuality quality) {
        if (filled != null && quality != null) WaterPurity.setQuality(filled.getFirst(), quality);
        return filled;
    }

    /**
     * The components water carries across every conversion Moonlight makes of its own accord. It copies
     * only what the soft fluid lists as preserved, and {@code moonlight:water} lists another thirst
     * mod's component, not this one's, so without this the grade is dropped the moment water becomes a
     * NeoForge fluid stack: a jar is an {@code IFluidHandler} there, so a pipe or a faucet pointed at a
     * tank moves its water through one.
     */
    public static HolderSet<DataComponentType<?>> preserving(SoftFluid fluid,
                                                             HolderSet<DataComponentType<?>> preserved) {
        if (!isWater(fluid)) return preserved;
        List<Holder<DataComponentType<?>>> types = new ArrayList<>(preserved.stream().toList());
        types.add(holder(ThirstComponents.WATER_PURITY));
        types.add(holder(ThirstComponents.WATER_SALTY));
        return HolderSet.direct(types);
    }

    private static Holder<DataComponentType<?>> holder(DataComponentType<?> type) {
        return BuiltInRegistries.DATA_COMPONENT_TYPE.wrapAsHolder(type);
    }
}
