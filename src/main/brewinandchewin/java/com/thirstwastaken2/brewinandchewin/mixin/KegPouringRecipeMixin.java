package com.thirstwastaken2.brewinandchewin.mixin;

import com.thirstwastaken2.brewinandchewin.KegWater;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;

/**
 * The fluid a pouring recipe pours from {@code container}. The recipe answers with its own plain water
 * whatever the bucket held, and the keg fills itself from that answer, compares it with what it already
 * holds, and picks the recipe by it. Answering with the container's grade makes all three follow the
 * grade: a Dirty bucket fills Dirty water, tops up a Dirty keg and is refused by a Clean one.
 */
@Mixin(value = KegPouringRecipe.class, remap = false)
abstract class KegPouringRecipeMixin {
    @Inject(method = "getFluid", at = @At("RETURN"), cancellable = true)
    private void thirst$gradeOfContainer(ItemStack container, CallbackInfoReturnable<AbstractedFluidStack> cir) {
        cir.setReturnValue(KegWater.pouredFrom(container, cir.getReturnValue()));
    }
}
