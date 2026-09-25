package com.thirstwastaken2.brewinandchewin.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.thirstwastaken2.brewinandchewin.KegWater;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;

/**
 * A graded water bottle poured into a keg. The bottle recipe is {@code strict}: it takes only a stack
 * with exactly its own components, a water bottle with nothing else, and every bottle this mod stamps
 * carries a grade. So where the keg compares the stack in hand with a recipe's stack, a stamped water
 * container also matches as its plain self; the grade then goes in through {@code getFluid}, see
 * {@link KegPouringRecipeMixin}.
 *
 * <p>Two places compare: the filter that picks the recipe ({@code getPouringRecipe}'s lambda, by its
 * synthetic name, the same in both loaders' jars) and {@code fluidExtract}, which checks again before
 * filling. {@code fluidExtract} also compares a drawn container with the output slot; a drawn container
 * is stamped itself, so that comparison is left exact and two grades never stack there.
 */
@Mixin(value = KegBlockEntity.class, remap = false)
abstract class KegBottleMixin {
    // Minecraft's own method, so remapped on Fabric, whose Brewin' and Chewin' jar is in intermediary.
    @WrapOperation(method = {"lambda$getPouringRecipe$4", "fluidExtract"},
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean thirst$matchGradedWater(ItemStack expected, ItemStack held, Operation<Boolean> original) {
        return original.call(expected, held)
                || KegWater.isGradedFormOf(expected, held) && original.call(expected, KegWater.plain(held));
    }
}
