package com.thirstwastaken2.createfly.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.thirstwastaken2.createfly.WaterFluids;
import com.thirstwastaken2.item.WaterContainers;
import com.thirstwastaken2.purity.WaterQuality;
import com.zurrtum.create.content.fluids.transfer.GenericItemFilling;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Bottles and buckets filled by a Spout, or by hand from a Create tank. The quality is read before the
 * fill runs, because the fill spends the fluid and a spent stack reads as empty.
 */
@Mixin(GenericItemFilling.class)
abstract class GenericItemFillingMixin {
    @WrapMethod(method = "fillItem")
    private static ItemStack thirst$stampFilled(Level level, int requiredAmount, ItemStack stack,
                                                FluidStack availableFluid, Operation<ItemStack> original) {
        WaterQuality quality = WaterFluids.isWater(availableFluid) ? WaterFluids.quality(availableFluid) : null;
        return WaterFluids.stampContainer(original.call(level, requiredAmount, stack, availableFluid), quality);
    }

    /**
     * Asks about one of a stack, as {@code fillItem} already fills one. Create Fly otherwise asks the
     * Transfer API about the whole stack through a context with no overflow slot, where filling one
     * terracotta bowl of two has nowhere to put the water bowl. The answer was then no room at all, and
     * a Spout over a stack of bowls filled up and never poured. Only this mod's containers, so another
     * mod's stackable item keeps whatever Create Fly gives it.
     */
    @WrapMethod(method = "getRequiredAmountForItem")
    private static int thirst$askAboutOne(Level level, ItemStack stack, FluidStack availableFluid,
                                          Operation<Integer> original) {
        boolean stacked = stack.getCount() > 1 && WaterContainers.handles(stack);
        return original.call(level, stacked ? stack.copyWithCount(1) : stack, availableFluid);
    }
}
