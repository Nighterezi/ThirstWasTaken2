package com.thirstwastaken2.supplementaries.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.datafixers.util.Pair;
import com.thirstwastaken2.purity.WaterQuality;
import com.thirstwastaken2.supplementaries.SoftFluidQuality;
import net.mehvahdjukaar.moonlight.api.fluids.FluidContainerList;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Every container a soft fluid tank fills: a bottle or bucket drawn from a jar or a goblet, and
 * whatever a faucet hands on. Moonlight builds the filled stack from scratch and copies components onto
 * it, which is not enough for this mod's own rules, so it is stamped the way every other container the
 * mod fills is.
 */
@Mixin(value = SoftFluidStack.class, remap = false)
abstract class SoftFluidStackMixin {
    @WrapMethod(method = "splitToItem")
    private Pair<ItemStack, FluidContainerList.Category> thirst$stampFilled(
            ItemStack emptyContainer, Operation<Pair<ItemStack, FluidContainerList.Category>> original) {
        SoftFluidStack fluid = (SoftFluidStack) (Object) this;
        // Read before the call: it spends the serving, and a stack spent to nothing reads as empty.
        WaterQuality quality = SoftFluidQuality.isWater(fluid) ? SoftFluidQuality.quality(fluid) : null;
        return SoftFluidQuality.filled(original.call(emptyContainer), quality);
    }
}
