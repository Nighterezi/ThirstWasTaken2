package com.thirstwastaken2.brewinandchewin.mixin;

import com.thirstwastaken2.brewinandchewin.HeldKegWater;
import com.thirstwastaken2.brewinandchewin.KegWater;
import com.thirstwastaken2.purity.WaterQuality;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;

/**
 * Lets the Jade reader ask a keg for the grade of its water. On the client the tank is the one
 * {@code writeUpdateTag} sent, components included, so the answer is the server's.
 *
 * <p>Its own mixin rather than part of {@code KegFermentingMixin}, so that the line stays when
 * {@code canFerment} moves upstream.
 */
@Mixin(value = KegBlockEntity.class, remap = false)
abstract class KegHeldWaterMixin implements HeldKegWater {
    @Shadow
    @Final
    private AbstractedFluidTank fluidTank;

    @Override
    public WaterQuality thirst$heldWater() {
        AbstractedFluidStack fluid = fluidTank.getAbstractedFluid();
        return KegWater.isWater(fluid) ? KegWater.quality(fluid) : null;
    }
}
