package com.thirstwastaken2.culturaldelights.mixin;

import com.baisylia.culturaldelights.block.entity.custom.VatBlockEntity;
import com.thirstwastaken2.culturaldelights.VatWater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Nothing brews from sea water, as in Brewin' and Chewin's keg. Every water recipe matches the item tag
 * {@code c:buckets/water}, which a sea water bucket is in, so without this the vat would turn it into
 * beer or cola as safe as any other. Fresh water of any grade still brews: the drink is its own item.
 *
 * <p>{@code hasRecipe} is what the tick asks each time, and on false it resets the progress, so a vat
 * already brewing stops when a sea water bucket is put in. The bucket can still sit in its slot, by hand
 * or by hopper; nothing starts until it is taken out.
 */
@Mixin(VatBlockEntity.class)
abstract class VatSeaWaterMixin {
    @Inject(method = "hasRecipe", at = @At("HEAD"), cancellable = true)
    private static void thirst$noBrewFromSeaWater(VatBlockEntity vat, CallbackInfoReturnable<Boolean> cir) {
        if (VatWater.holdsSeaWater(vat)) cir.setReturnValue(false);
    }
}
