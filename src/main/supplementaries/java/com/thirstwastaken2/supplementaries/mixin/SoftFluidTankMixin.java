package com.thirstwastaken2.supplementaries.mixin;

import com.thirstwastaken2.supplementaries.SoftFluidDrinking;
import com.thirstwastaken2.supplementaries.SoftFluidQuality;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The tank inside a jar, a goblet or a faucet: everything that goes into one, and drinking out of one.
 */
@Mixin(value = SoftFluidTank.class, remap = false)
abstract class SoftFluidTankMixin {
    /**
     * Water entering a tank is stamped with the grade it already reads as, before the tank decides
     * whether it fits: a tank compares components, so gradeless water would refuse to share with water
     * that has a grade although both read the same to everything else.
     */
    @Inject(method = "addFluid", at = @At("HEAD"))
    private void thirst$stampAddedWater(SoftFluidStack stack, boolean simulate, CallbackInfoReturnable<Integer> cir) {
        SoftFluidQuality.normalise(stack);
    }

    /**
     * Water has no food item, so Moonlight would refuse to drink it and both blocks would do nothing.
     * Injected at the head, before that test, so no food entry is needed and no other fluid changes.
     */
    @Inject(method = "tryDrinkUpFluid", at = @At("HEAD"), cancellable = true)
    private void thirst$drinkWater(Player player, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (SoftFluidDrinking.drink((SoftFluidTank) (Object) this, player, level)) cir.setReturnValue(true);
    }
}
