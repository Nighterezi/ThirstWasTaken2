package com.thirstwastaken2.brewinandchewin.mixin;

import com.thirstwastaken2.brewinandchewin.KegWater;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;

/**
 * Nothing ferments from sea water. The keg stores it and hands it back salty, as the stockpot does, but
 * every water recipe matches the fluid tag {@code #c:water}, which ignores components, so without this
 * a keg of sea water would brew beer as safe as any other. Fresh water of any grade still ferments:
 * a brewed drink is safe whatever went in, as tea is.
 *
 * <p>{@code canFerment} is where the tick asks each time, so a keg already brewing stops when sea water
 * is in it. The full descriptor names only the mod's own types, so it is the same on both loaders. The
 * keg it is asked about is always this one; its tank is read through the shadow, since calling the keg
 * would need Farmer's Delight, which it extends, on the compile classpath.
 */
@Mixin(value = KegBlockEntity.class, remap = false)
abstract class KegFermentingMixin {
    @Shadow
    @Final
    private AbstractedFluidTank fluidTank;

    @Inject(method = "canFerment(Lumpaz/brewinandchewin/common/crafting/KegFermentingRecipe;Lumpaz/brewinandchewin/common/block/entity/KegBlockEntity;)Z",
            at = @At("HEAD"), cancellable = true)
    private void thirst$noBrewFromSeaWater(KegFermentingRecipe recipe, KegBlockEntity keg, CallbackInfoReturnable<Boolean> cir) {
        if (KegWater.isSalt(fluidTank.getAbstractedFluid())) cir.setReturnValue(false);
    }
}
