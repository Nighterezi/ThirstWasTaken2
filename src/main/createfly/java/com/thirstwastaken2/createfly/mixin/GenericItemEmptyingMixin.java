package com.thirstwastaken2.createfly.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.thirstwastaken2.createfly.WaterFluids;
import com.thirstwastaken2.item.WaterContainers;
import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.fluids.transfer.GenericItemEmptying;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Bottles and buckets poured into an Item Drain, or by hand into a Create tank. The quality is read
 * before emptying, which shrinks the stack it would be read from.
 *
 * <p>The waterskin and the terracotta water bowl are emptied here rather than by Create Fly. It only
 * drains an item that holds a whole bucket, and hands back nothing for one that holds less, while still
 * saying the item can be emptied: an Item Drain took a waterskin in and deleted it.
 */
@Mixin(GenericItemEmptying.class)
abstract class GenericItemEmptyingMixin {
    /** A serving in Create Fly's droplets: 250 mB, 81 each. */
    private static final int SERVING = 250 * 81;

    @WrapMethod(method = "emptyItem")
    private static Pair<FluidStack, ItemStack> thirst$stampEmptied(BlockAndLightGetter level, ItemStack stack,
                                                                   boolean simulate,
                                                                   Operation<Pair<FluidStack, ItemStack>> original) {
        int servings = WaterContainers.handles(stack) ? WaterContainers.servings(stack) : 0;
        if (servings > 0) {
            WaterQuality quality = WaterPurity.quality(stack);
            ItemStack emptied = WaterContainers.holding(stack, quality, 0);
            if (!simulate) stack.shrink(1);
            return Pair.of(WaterFluids.stamp(new FluidStack(Fluids.WATER, servings * SERVING), quality), emptied);
        }
        WaterQuality quality = WaterPurity.isWaterContainer(stack) ? WaterPurity.quality(stack) : null;
        Pair<FluidStack, ItemStack> result = original.call(level, stack, simulate);
        if (quality == null || !WaterFluids.isWater(result.getFirst())) return result;
        // A copy, because an emptying recipe hands out its own result stack.
        return Pair.of(WaterFluids.stamp(result.getFirst().copy(), quality), result.getSecond());
    }
}
