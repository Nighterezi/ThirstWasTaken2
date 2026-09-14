package com.thirstwastaken2.createfly.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.thirstwastaken2.createfly.WaterFluids;
import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.fluids.transfer.GenericItemEmptying;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndLightGetter;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Bottles and buckets poured into an Item Drain, or by hand into a Create tank. The quality is read
 * before emptying, which shrinks the stack it would be read from.
 */
@Mixin(GenericItemEmptying.class)
abstract class GenericItemEmptyingMixin {
    @WrapMethod(method = "emptyItem")
    private static Pair<FluidStack, ItemStack> thirst$stampEmptied(BlockAndLightGetter level, ItemStack stack,
                                                                   boolean simulate,
                                                                   Operation<Pair<FluidStack, ItemStack>> original) {
        WaterQuality quality = WaterPurity.isWaterContainer(stack) ? WaterPurity.quality(stack) : null;
        Pair<FluidStack, ItemStack> result = original.call(level, stack, simulate);
        if (quality == null || !WaterFluids.isWater(result.getFirst())) return result;
        // A copy, because an emptying recipe hands out its own result stack.
        return Pair.of(WaterFluids.stamp(result.getFirst().copy(), quality), result.getSecond());
    }
}
