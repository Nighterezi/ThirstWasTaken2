package com.thirstwastaken2.createfly.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.thirstwastaken2.createfly.WaterFluids;
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
}
