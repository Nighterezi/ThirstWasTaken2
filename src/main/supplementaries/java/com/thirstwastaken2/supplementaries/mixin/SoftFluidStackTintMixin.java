package com.thirstwastaken2.supplementaries.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.thirstwastaken2.supplementaries.SoftFluidQuality;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The colour a jar, a goblet or a faucet draws its water in. Moonlight asks the biome, which is the
 * right answer for water in the world and says nothing about water in a jar; here the grade decides,
 * so a jar of dirty water looks dirty and a jar of sea water is turquoise.
 *
 * <p>Only the still colour is taken. Moonlight works the flowing colour and the particle colour out
 * from it for a fluid tinted both ways, which water is, so one hook answers for all three. The tank
 * that holds the fluid caches what it is told and drops the cache when its contents change, and a
 * grade is part of its contents, so nothing goes stale.
 *
 * <p>Separate from {@code SoftFluidStackMixin} because this one is only ever asked on a client, while
 * that one runs wherever water is poured.
 */
@Mixin(value = SoftFluidStack.class, remap = false)
abstract class SoftFluidStackTintMixin {
    @ModifyReturnValue(method = "getStillColor", at = @At("RETURN"))
    private int thirst$tintByGrade(int original) {
        return SoftFluidQuality.tint((SoftFluidStack) (Object) this, original);
    }
}
